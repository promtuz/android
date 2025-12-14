//! ## Creating JNI ready external functions
//!
//!
//! for eg.
//! `getStaticKey` method on class `Crypto` at package `com.promtuz.core`
//! will have name `Java_com_promtuz_core_Crypto_getStaticKey``
//!
//! ```rs
//! #[jni(base = "com.promtuz.core", class = "Crypto")]
//! pub extern "system" fn getStaticKey(
//!   mut env: JNIEnv,
//!   _class: JClass,
//!   // Further Arguments
//! ) {
//!   // Body
//! }
//! ```

use std::sync::Arc;
use std::sync::Mutex;
use std::sync::RwLock;

use common::msg::cbor::FromCbor;
use common::msg::cbor::ToCbor;
use common::msg::client::ClientRequest;
use common::msg::client::ClientResponse;
use common::quic::config::build_client_cfg;
use common::quic::config::load_root_ca_bytes;
use common::quic::config::setup_crypto_provider;
use common::quic::protorole::ProtoRole;
use jni::JNIEnv;
use jni::objects::JClass;
use jni::objects::JObject;
use log::error;
use log::info;
use macros::jni;
use once_cell::sync::Lazy;
use once_cell::sync::OnceCell;
use quinn::Connection;
use quinn::Endpoint;
use quinn::EndpointConfig;
use quinn::TransportConfig;
use quinn::VarInt;
use quinn::default_runtime;
use rusqlite as sql;
use tokio::io::AsyncReadExt;
use tokio::io::AsyncWriteExt;
use tokio::runtime::Runtime;
use tokio::sync::mpsc;

use crate::data::ResolverSeeds;
use crate::data::relay::Relay;
use crate::db::NETWORK_DB;
use crate::events::InternalEvent;
use crate::events::connection::ConnectionState;
use crate::quic::dialer::connect_to_any_seed;
use crate::utils::has_internet;
use crate::utils::sqlite::initial_execute;
use crate::utils::ujni::get_package_name;
use crate::utils::ujni::read_raw_res;

mod crypto;
mod data;
mod db;
mod events;
mod identity;
mod quic;
mod utils;

type JE<'local> = JNIEnv<'local>;
type JC<'local> = JClass<'local>;

/// App's Package Name
static PACKAGE_NAME: &str = "com.promtuz.chat";

/// Event Bus for
/// Rust -> Kotlin
static EVENT_BUS: Lazy<(
    mpsc::UnboundedSender<InternalEvent>,
    Mutex<mpsc::UnboundedReceiver<InternalEvent>>,
)> = Lazy::new(|| {
    let (tx, rx) = mpsc::unbounded_channel();
    (tx, Mutex::new(rx))
});

/// Global Tokio Runtime
pub static RUNTIME: Lazy<Runtime> = Lazy::new(|| Runtime::new().unwrap());

pub static ENDPOINT: OnceCell<Endpoint> = OnceCell::new();

/// Connection to any relay server
pub static CONNECTION: OnceCell<Mutex<Connection>> = OnceCell::new();

#[macro_export]
macro_rules! endpoint {
    () => {
        if let Some(ep) = $crate::ENDPOINT.get() {
            ep
        } else {
            log::error!("API is not initialized.");
            return;
        }
    };
}

/// Entry point for API
///
/// Initializes Endpoint
#[jni(base = "com.promtuz.core", class = "API")]
pub extern "system" fn initApi(mut env: JNIEnv, _: JC, context: JObject) {
    info!("API: INIT START");

    let rt = RUNTIME.handle().clone();
    let _guard = rt.enter();

    let socket = std::net::UdpSocket::bind("0.0.0.0:0").unwrap();

    let mut endpoint =
        Endpoint::new(EndpointConfig::default(), None, socket, default_runtime().unwrap()).unwrap();

    if let Ok(addr) = endpoint.local_addr() {
        info!("API: ENDPOINT BIND TO {}", addr);
    }

    jni_try!(env, setup_crypto_provider());

    let root_ca_bytes = jni_try!(env, read_raw_res(&mut env, &context, "root_ca"));
    let roots = jni_try!(env, load_root_ca_bytes(&root_ca_bytes));

    let mut client_cfg = jni_try!(env, build_client_cfg(ProtoRole::Client, &roots));

    client_cfg.transport_config(Arc::new(TransportConfig::default()));

    endpoint.set_default_client_config(client_cfg);

    ENDPOINT.set(endpoint).expect("init was ran twice");

    //==||==||==||==||==||==||==||==||==||==||==||==||==//
    info!("DB: STARTING SQLITE DATABASE");

    let db_block = (|| {
        info!("DB: INITIALIZING TABLES");
        initial_execute()?;

        Ok::<(), anyhow::Error>(())
    })();

    jni_try!(env, db_block);
}

/// Connects to Relay
#[jni(base = "com.promtuz.core", class = "API")]
pub extern "system" fn connect(mut env: JNIEnv, _: JC, context: JObject) {
    info!("API: CONNECTING");

    // Checking Internet Connectivity
    if !has_internet() {
        _ = EVENT_BUS.0.send(InternalEvent::Connection { state: ConnectionState::NoInternet });
        return;
    }

    let seeds = jni_try!(env, read_raw_res(&mut env, &context, "resolver_seeds"));
    let seeds = jni_try!(env, serde_json::from_slice::<ResolverSeeds>(&seeds)).seeds;

    RUNTIME.spawn(async move {
        match Relay::fetch_best() {
            Ok(relay) => {
                _ = relay.connect().await;
            },
            Err(rusqlite::Error::QueryReturnedNoRows) => {
                Relay::resolve(&seeds).await;
            },
            Err(err) => {
                error!("DB: Relay fetch best failed - {err}")
            },
        }
    });
}

#[jni(base = "com.promtuz.core", class = "Core")]
pub extern "system" fn initLogger(_: JE, _: JC) {
    android_logger::init_once(
        android_logger::Config::default().with_max_level(log::LevelFilter::Debug).with_tag("core"),
    );
}

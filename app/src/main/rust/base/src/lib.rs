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
use std::time::Duration;

use common::quic::config::build_client_cfg;
use common::quic::config::load_root_ca_bytes;
use common::quic::config::setup_crypto_provider;
use common::quic::protorole::ProtoRole;
use jni::JNIEnv;
use jni::objects::JByteArray;
use jni::objects::JClass;
use jni::objects::JObject;
use log::error;
use log::info;
use macros::jni;
use once_cell::sync::Lazy;
use once_cell::sync::OnceCell;
use parking_lot::RwLock;
use quinn::Connection;
use quinn::Endpoint;
use quinn::EndpointConfig;
use quinn::TransportConfig;
use quinn::default_runtime;
use tokio::runtime::Runtime;
use tokio::sync::mpsc;

use crate::data::ResolverSeeds;
use crate::data::relay::Relay;
use crate::db::initial_execute;
use crate::events::InternalEvent;
use crate::events::connection::ConnectionState;
use crate::quic::server::KeyPair;
use crate::quic::server::RelayConnError;
use crate::utils::KeyConversion;
use crate::utils::has_internet;
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
///
/// TODO: make abstractions for easily pushing events
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

/// current connection to any relay server,
/// could be none if not connection yet
pub static CONNECTION: RwLock<Option<Connection>> = RwLock::new(None);

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

    let mut transport_cfg = TransportConfig::default();
    transport_cfg.keep_alive_interval(Some(Duration::from_secs(15)));

    client_cfg.transport_config(Arc::new(transport_cfg));

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
pub extern "system" fn connect(
    mut env: JNIEnv,
    _: JC,
    context: JObject,
    ipk: JByteArray,
    // SECURITY: idk, i feel like i should be concerned
    isk: JByteArray,
) {
    info!("API: CONNECTING");

    // Checking Internet Connectivity
    if !has_internet() {
        _ = EVENT_BUS.0.send(InternalEvent::Connection { state: ConnectionState::NoInternet });
        return;
    }

    let seeds = jni_try!(env, read_raw_res(&mut env, &context, "resolver_seeds"));
    let seeds = jni_try!(env, serde_json::from_slice::<ResolverSeeds>(&seeds)).seeds;

    let ipk = ipk.to_public(&mut env);
    let isk = isk.to_secret(&mut env);

    let keypair = KeyPair { public: ipk, secret: isk };

    RUNTIME.spawn(async move {
        loop {
            info!("RELAY(BEST): Fetching");
            match Relay::fetch_best() {
                Ok(relay) => {
                    info!("RELAY(BEST): Found [{}]", relay.id);
                    match relay.connect(&keypair).await {
                        Ok(_) => break,
                        Err(RelayConnError::Continue) => continue,
                        Err(RelayConnError::Error(err)) => {
                            error!("RELAY({}): Connection failed - {:?}", relay.id, err);
                        },
                    }
                },
                Err(rusqlite::Error::QueryReturnedNoRows) => {
                    info!("RELAY(BEST): Not Found, Resolving");
                    match Relay::resolve(&seeds).await {
                        Ok(_) => continue,
                        Err(err) => {
                            error!("RESOLVE: Failed {err}");
                        },
                    }
                },
                Err(err) => {
                    error!("DB: Relay fetch best failed - {err}")
                },
            }

            break;
        }
    });
}

#[jni(base = "com.promtuz.core", class = "Core")]
pub extern "system" fn initLogger(_: JE, _: JC) {
    android_logger::init_once(
        android_logger::Config::default().with_max_level(log::LevelFilter::Trace).with_tag("core"),
    );
}

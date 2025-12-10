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

use std::panic;

use anyhow::anyhow;
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
use quinn::VarInt;
use quinn::default_runtime;
use tokio::io::AsyncReadExt;
use tokio::io::AsyncWriteExt;
use tokio::runtime::Runtime;

use crate::data::ResolverSeeds;
use crate::quic::dialer::connect_to_any_seed;
use crate::utils::ujni::get_package_name;
use crate::utils::ujni::get_raw_res_id;
use crate::utils::ujni::read_raw_res;

mod crypto;
mod data;
mod quic;
mod utils;

type JE<'local> = JNIEnv<'local>;
type JC<'local> = JClass<'local>;

/// Global Tokio Runtime
pub static RUNTIME: Lazy<Runtime> = Lazy::new(|| Runtime::new().unwrap());

pub static ENDPOINT: OnceCell<Endpoint> = OnceCell::new();

/// Connection to any relay server
pub static CONNECTION: OnceCell<Connection> = OnceCell::new();

macro_rules! endpoint {
    () => {
        if let Some(ep) = ENDPOINT.get() {
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
    info!("API: ROOT CA - {}", hex::encode(&root_ca_bytes));
    let roots = jni_try!(env, load_root_ca_bytes(&root_ca_bytes));
    info!("API: GOT CERT ROOTS {:?}", roots);

    info!("Root count: {}", roots.len());
    for (i, cert) in roots.roots.iter().enumerate() {
        info!("root[{}]: {:?}", i, cert.subject);
    }

    let client_cfg = jni_try!(env, build_client_cfg(ProtoRole::Client, &roots));
    endpoint.set_default_client_config(client_cfg);

    info!("API: UPDATED DEFAULT CLIENT CONFIG");

    ENDPOINT.set(endpoint).expect("init was ran twice");
}

/// Resolves Relays
///
/// Need Resolver Seeds
#[jni(base = "com.promtuz.core", class = "API")]
pub extern "system" fn resolve(mut env: JNIEnv, _: JC, context: JObject) {
    info!("API: RESOLVING");

    let package_name: String = jni_try!(env, get_package_name(&mut env, &context));
    info!("API: PACKAGE {}", package_name);

    let seeds = jni_try!(env, read_raw_res(&mut env, &context, "resolver_seeds"));

    let seeds = jni_try!(env, serde_json::from_slice::<ResolverSeeds>(&seeds)).seeds;

    RUNTIME.spawn(async move {
        info!("SET STATE = RESOLVING");

        let conn = match connect_to_any_seed(endpoint!(), &seeds).await {
            Ok(conn) => conn,
            Err(err) => {
                info!("SET STATE = FAILED");
                error!("API: RESOLVER FAILED - {}", err);
                return;
            },
        };

        let req = ClientRequest::GetRelays().pack().unwrap();

        info!("API: RESOLVER SENDING {}", hex::encode(&req));

        if let Ok((mut send, mut recv)) = conn.open_bi().await {
            _ = send.write_all(&req).await;
            _ = send.flush().await;

            use CRes::*;
            use ClientResponse as CRes;

            if let Ok(packet_size) = recv.read_u32().await {
                info!("API: PACKET SIZE({})", packet_size);

                let mut packet = vec![0u8; packet_size as usize];

                if let Err(err) = recv.read_exact(&mut packet).await {
                    error!("Read failed : {}", err); // temp
                }

                info!("API: PACKET({})", hex::encode(&packet));

                match CRes::from_cbor(&packet) {
                    Ok(cres) => {
                        #[allow(irrefutable_let_patterns)]
                        if let GetRelays { relays } = cres {
                            info!("GOT RELAYS : {:?}", relays);

                            conn.close(VarInt::from_u32(1), &[]);
                        }
                    },
                    Err(err) => {
                        error!("API: CLIENT RES DECODE ERR : {}", err);
                    },
                }

                // if let Ok(GetRelays { relays }) = CRes::from_cbor(&packet) {
                //     info!("GOT RELAYS : {:?}", relays);
                // } else {

                // }
            }
        }
    });
}

#[jni(base = "com.promtuz.core", class = "Core")]
pub extern "system" fn initLogger(_: JE, _: JC) {
    android_logger::init_once(
        android_logger::Config::default().with_max_level(log::LevelFilter::Debug).with_tag("core"),
    );
}

use std::sync::Mutex;

use jni::JNIEnv;
use jni::objects::JClass;
use once_cell::sync::Lazy;
use once_cell::sync::OnceCell;
use parking_lot::RwLock;
use quinn::Connection;
use quinn::Endpoint;
use tokio::runtime::Runtime;
use tokio::sync::mpsc;

use crate::events::InternalEvent;

mod api;
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

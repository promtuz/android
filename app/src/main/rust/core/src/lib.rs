//! ## Creating JNI ready external functions
//!
//!
//! for eg.
//! `getStaticKey` method on class `Crypto` at package `com.promtuz.core`
//! will have name `Java_com_promtuz_core_Crypto_getStaticKey``
//!
//! for eg.
//! ```rs
//! #[jni(base = "com.promtuz.core", class = "Crypto")]
//! pub extern "C" fn getStaticKey(
//!   mut env: JNIEnv,
//!   _class: JClass,
//!   // Further Arguments
//! ) {
//!   // Body
//! }
//! ```

use jni::{JNIEnv, objects::JClass};
use macros::jni;
pub mod utils;
pub mod crypto;

#[jni(base = "com.promtuz.core", class = "Core")]
pub extern "C" fn initLogger<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) {
    android_logger::init_once(
        android_logger::Config::default()
            .with_max_level(log::LevelFilter::Debug)
            .with_tag("libcore"),
    );
}
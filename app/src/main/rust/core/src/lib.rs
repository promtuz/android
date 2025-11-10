//! ## Creating JNI ready external functions
//!
//!
//! for eg.
//! `getStaticKey` method on class `Core` at package `com.promtuz.rust`
//! will have name `Java_com_promtuz_rust_Core_getStaticKey``
//!
//! for eg.
//! ```rs
//! #[jni(base = "com.promtuz.rust", class = "Core")]
//! pub extern "C" fn getStaticKey(
//!   mut env: JNIEnv,
//!   _class: JClass,
//!   // Further Arguments
//! ) {
//!   // Body
//! }
//! ```

use jni::JNIEnv;
use jni::objects::{JClass, JObject, JValue};
use jni::sys::jobject;

use crate::utils::get_pair_object;
pub mod utils;

use libcore::get_static_keypair;
use macros::jni;

#[jni(base = "com.promtuz.rust", class = "Core")]
pub extern "C" fn getStaticKeypair(
    mut env: JNIEnv, 
    _class: JClass
) -> jobject {
    let (secret, public) = get_static_keypair();

    let secret_bytes = secret.to_bytes();
    let public_bytes = public.to_bytes();

    let secret_jarray = env.byte_array_from_slice(&secret_bytes).unwrap();
    let public_jarray = env.byte_array_from_slice(&public_bytes).unwrap();

    get_pair_object(
        &mut env,
        JValue::Object(&JObject::from(secret_jarray)),
        JValue::Object(&JObject::from(public_jarray)),
    )
}

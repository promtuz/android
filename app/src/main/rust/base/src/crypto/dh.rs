use std::error::Error;

use common::crypto::EphemeralSecret;
use jni::JNIEnv;
use jni::objects::JByteArray;
use jni::objects::JClass;
use jni::objects::JObject;
use jni::signature::TypeSignature;
use jni::sys::jlong;
use jni::sys::jobject;
use macros::jni;

use crate::utils::KeyConversion;

#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "system" fn ephemeralDiffieHellman<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    ephemeral_secret_ptr: jlong,
    public_key_bytes: JByteArray,
) -> JByteArray<'local> {
    let esk = unsafe { *Box::from_raw(ephemeral_secret_ptr as *mut EphemeralSecret) };
    let public_key = public_key_bytes.to_public(&mut env);

    let shared_key = esk.diffie_hellman(&public_key);
    let shared_jarray = env.byte_array_from_slice(&shared_key.to_bytes()).unwrap();

    JByteArray::from(shared_jarray)
}

/// Assuming class looks like
///
/// ```kt
/// package com.promtuz.chat.security
///
/// class StaticSecret(
///    private val key: ByteArray,
/// ) {
///    private var used = false
///    ...
/// ```
#[jni(base = "com.promtuz.chat.security", class = "StaticSecret")]
pub extern "system" fn diffieHellman<'local>(
    mut env: JNIEnv<'local>, class: JClass, public_key_bytes: JByteArray,
) -> jobject {
    let key = (|| {
        let key_obj = env.get_field(&class, "key", "[B")?.l()?;
        Ok::<_, jni::errors::Error>(JByteArray::from(key_obj))
    })()
    .expect("shouldn't happen");

    if !env.get_field(&class, "used", "Z").unwrap().z().unwrap()
        && env.set_field(&class, "used", "Z", true.into()).is_ok()
    {
        let secret_key = key.to_secret(&mut env);
        let public_key = public_key_bytes.to_public(&mut env);

        let shared_key = secret_key.diffie_hellman(&public_key);
        let shared_jarray = env.byte_array_from_slice(&shared_key.to_bytes()).unwrap();

        JByteArray::from(shared_jarray).as_raw()
    } else {
        JObject::null().as_raw()
    }
}

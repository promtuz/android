use libcore::EphemeralSecret;
use jni::{
    JNIEnv,
    objects::{JByteArray, JClass},
    sys::jlong,
};
use macros::jni;

use crate::utils::KeyConversion;

#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "C" fn ephemeralDiffieHellman<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    // pointer to EphemeralSecret
    // idk about lifetimes so i have no idea how long will this pointer live
    // afaik box pointers live throughout entire process lifetime cuz they're stored in the heap
    ephemeral_secret_ptr: jlong,
    public_key_bytes: JByteArray,
) -> JByteArray<'local> {
    let esk = unsafe { *Box::from_raw(ephemeral_secret_ptr as *mut EphemeralSecret) };
    let public_key = public_key_bytes.to_public(&mut env);

    let shared_key = esk.diffie_hellman(&public_key);
    let shared_jarray = env.byte_array_from_slice(&shared_key.to_bytes()).unwrap();

    JByteArray::from(shared_jarray)
}

#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "C" fn diffieHellman<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    secret_key_bytes: JByteArray,
    public_key_bytes: JByteArray,
) -> JByteArray<'local> {
    let secret_key = secret_key_bytes.to_secret(&mut env);
    let public_key = public_key_bytes.to_public(&mut env);

    let shared_key = secret_key.diffie_hellman(&public_key);
    let shared_jarray = env.byte_array_from_slice(&shared_key.to_bytes()).unwrap();

    JByteArray::from(shared_jarray)
}

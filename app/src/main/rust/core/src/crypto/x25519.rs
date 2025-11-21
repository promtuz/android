use common::crypto::{get_ephemeral_keypair, get_shared_key, get_static_keypair};
use jni::{
    JNIEnv,
    objects::{JByteArray, JClass, JObject, JString, JValue},
    sys::{jlong, jobject},
};
use macros::jni;

use crate::utils::{KeyConversion, get_pair_object};

#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "system" fn getStaticKeypair(
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


#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "system" fn getEphemeralKeypair<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jobject {
    let res = std::panic::catch_unwind(move || {
        let (esk, epk) = get_ephemeral_keypair();
        log::debug!("Got Epehem KP? {:?}", epk);
        let esk_ptr = Box::new(esk);
        let public_jarray = env
            .byte_array_from_slice(epk.as_bytes())
            .expect("Failed to allocate public buffer");
        log::debug!("Jarray {:?}", public_jarray);

        let jarray_obj: JObject<'local> = JObject::from(public_jarray);

        let long_class = env.find_class("java/lang/Long").unwrap();
        let long_obj = env
            .new_object(
                long_class,
                "(J)V",
                &[JValue::Long(Box::into_raw(esk_ptr) as jlong)],
            )
            .unwrap();

        get_pair_object(
            &mut env,
            JValue::Object(&long_obj),
            JValue::Object(&jarray_obj),
        )
    });

    match res {
        Ok(o) => o,
        Err(err) => {
            log::error!("Panic on getEphemeralKeypair : {:#?}", err);
            JObject::null().as_raw()
        }
    }
}

#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "system" fn deriveSharedKey<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass,
    raw_key: JByteArray<'local>,
    salt: JByteArray<'local>,
    info: JString<'local>,
) -> JByteArray<'local> {
    let salt = env.convert_byte_array(salt).unwrap();
    let info: String = env.get_string(&info).unwrap().into();

    let raw_shared_key_slice = raw_key.to_bytes(&env);

    let shared_key = get_shared_key(&raw_shared_key_slice, &salt, &info);

    env.byte_array_from_slice(&shared_key).unwrap()
}

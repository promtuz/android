use common::crypto::StaticSecret;
use common::crypto::get_ephemeral_keypair;
use common::crypto::get_shared_key;
use common::crypto::get_static_keypair;
use common::crypto::sign::derive_ed25519;
use jni::JNIEnv;
use jni::objects::AsJArrayRaw;
use jni::objects::JByteArray;
use jni::objects::JClass;
use jni::objects::JObject;
use jni::objects::JString;
use jni::objects::JValue;
use jni::objects::JValueGen;
use jni::sys::jlong;
use jni::sys::jobject;
use macros::jni;

use crate::utils::KeyConversion;
use crate::utils::get_pair_object;

#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "system" fn getStaticKeypair(mut env: JNIEnv, _class: JClass) -> jobject {
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
    mut env: JNIEnv<'local>, _class: JClass<'local>,
) -> jobject {
    let res = std::panic::catch_unwind(move || {
        let (esk, epk) = get_ephemeral_keypair();
        log::debug!("Got Epehem KP? {:?}", epk);
        let esk_ptr = Box::new(esk);
        let public_jarray =
            env.byte_array_from_slice(epk.as_bytes()).expect("Failed to allocate public buffer");
        log::debug!("Jarray {:?}", public_jarray);

        let jarray_obj: JObject<'local> = JObject::from(public_jarray);

        let long_class = env.find_class("java/lang/Long").unwrap();
        let long_obj = env
            .new_object(long_class, "(J)V", &[JValue::Long(Box::into_raw(esk_ptr) as jlong)])
            .unwrap();

        get_pair_object(&mut env, JValue::Object(&long_obj), JValue::Object(&jarray_obj))
    });

    match res {
        Ok(o) => o,
        Err(err) => {
            log::error!("Panic on getEphemeralKeypair : {:#?}", err);
            JObject::null().as_raw()
        },
    }
}

#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "system" fn deriveSharedKey<'local>(
    mut env: JNIEnv<'local>, _class: JClass, raw_key: JByteArray<'local>, salt: JByteArray<'local>,
    info: JString<'local>,
) -> JByteArray<'local> {
    let salt = env.convert_byte_array(salt).unwrap();
    let info: String = env.get_string(&info).unwrap().into();

    let raw_shared_key_slice = raw_key.to_bytes(&env);

    let shared_key = get_shared_key(&raw_shared_key_slice, &salt, &info);

    env.byte_array_from_slice(&shared_key).unwrap()
}

/// Derives signing key from secret key
#[jni(base = "com.promtuz.chat.security", class = "StaticSecret")]
pub extern "system" fn toSigningKey(mut env: JNIEnv, class: JClass) -> jobject {
    (|| {
        let pkey_obj = env.get_field(class, "key", "[B")?.l()?;
        let key: [u8; 32] = JByteArray::from(pkey_obj).to_bytes(&env);

        let skey = env.byte_array_from_slice(derive_ed25519(&key).as_bytes())?;
        let skey_obj = unsafe { JObject::from_raw(skey.as_jarray_raw()) };

        let sign_key_class = env.find_class("com/promtuz/chat/security/SigningKey")?;
        let inst = env.new_object(sign_key_class, "([B)V", &[(&skey_obj).into()])?;

        Ok::<*mut jni::sys::_jobject, jni::errors::Error>(inst.as_raw())
    })()
    .unwrap_or(JObject::null().as_raw())
}

///
/// `external fun getVerificationKey(): ByteArray`
#[jni(base = "com.promtuz.chat.security", class = "SigningKey")]
pub extern "system" fn getVerificationKey(mut env: JNIEnv, class: JClass) -> jobject {
    (|| {
        let key_obj = env.get_field(class, "key", "[B")?.l()?;
        let key = JByteArray::from(key_obj).to_signing(&mut env);

        let arr = env.byte_array_from_slice(key.verifying_key().as_bytes())?;

        Ok::<*mut jni::sys::_jobject, jni::errors::Error>(arr.as_raw())
    })()
    .unwrap_or(JObject::null().as_raw())
}

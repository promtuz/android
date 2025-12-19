use common::crypto::encrypt::Encrypted;
use jni::JNIEnv;
use jni::objects::JByteArray;
use jni::objects::JClass;
use jni::objects::JObject;
use jni::sys::jobject;
use macros::jni;

use crate::utils::KeyConversion;
use crate::utils::ToJObject;

#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "system" fn decryptData<'local>(
    env: JNIEnv<'local>, _class: JClass, cipher: JByteArray<'local>, nonce: JByteArray<'local>,
    key: JByteArray<'local>, ad: JByteArray<'local>,
) -> JByteArray<'local> {
    let encrypted = Encrypted {
        cipher: env.convert_byte_array(cipher).unwrap(),
        nonce: env.convert_byte_array(nonce).unwrap(),
    };
    let key = key.to_bytes(&env);
    let ad = env.convert_byte_array(ad).unwrap();

    let data = match encrypted.decrypt(&key, &ad) {
        Ok(dat) => dat,
        Err(err) => {
            log::error!("DecryptData Fail : {}", err);
            vec![0u8]
        },
    };

    let jarray = env.byte_array_from_slice(&data).unwrap();

    JByteArray::from(jarray)
}

#[jni(base = "com.promtuz.core", class = "Crypto")]
pub extern "system" fn encryptData<'local>(
    mut env: JNIEnv<'local>, _class: JClass, data: JByteArray<'local>, key: JByteArray<'local>,
    ad: JByteArray<'local>,
) -> jobject {
    let data = env.convert_byte_array(data).unwrap();
    let key = key.to_bytes(&env);
    let ad = env.convert_byte_array(ad).unwrap();
    let encrypted = Encrypted::encrypt(&data, &key, &ad);

    encrypted.to_jobject(&mut env).unwrap_or_else(|_| JObject::null().as_raw())
}

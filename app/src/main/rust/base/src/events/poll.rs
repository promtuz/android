use common::msg::cbor::ToCbor;
use jni::JNIEnv;
use jni::objects::JClass;
use jni::objects::JObject;
use jni::sys::jbyteArray;
use log::info;
use macros::jni;

use crate::EVENT_BUS;

/// Event polling rate in ms
///
/// 16.677ms (1000ms/60fps)
// static POLLING_RATE: u32 = 16;

#[jni(base = "com.promtuz.core", class = "API")]
pub extern "system" fn pollEvent(env: JNIEnv, _class: JClass) -> jbyteArray {
    let mut rx = EVENT_BUS.1.lock().unwrap();

    match rx.try_recv() {
        Ok(ev) => {
            let bytes = ev.to_cbor().unwrap();

            info!("EVENT_POLL: {:?}", ev);

            env.byte_array_from_slice(&bytes).unwrap().as_raw()
        },
        Err(_) => JObject::null().as_raw(), // no event available
    }
}

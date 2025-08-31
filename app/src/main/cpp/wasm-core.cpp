#include <jni.h>
#include <cstdint>

#include "core_bindings.h"


extern "C" {
JNIEXPORT jbyteArray JNICALL
Java_com_promtuz_chat_nativex_CoreBridge_getStaticKey(JNIEnv *env, jobject thiz) {
    jbyteArray arr = env->NewByteArray(32);
    jbyte *buf = env->GetByteArrayElements(arr, nullptr);
    c_get_static_key(reinterpret_cast<uint8_t *>(buf));
    env->ReleaseByteArrayElements(arr, buf, 0);

    return arr;
}
}
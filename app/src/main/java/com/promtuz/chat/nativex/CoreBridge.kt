package com.promtuz.chat.nativex

class CoreBridge {
    companion object {
        init {
            System.loadLibrary("wasm_core")

            println("LOOOOOOOOOOOOOOOADDDDDDDDDDDDDDDDDDDDDDDDEDDDDDDDDDDDD THE FUCKINNNNNNNNNNNNNNNNNNNNNNNNNGGGGGGGGGGGGGGGGGGGGGGGG LIBRARRRRRRRRRRRRRRRRRRRRRRRRRRYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY")
        }

        @Volatile
        private var INSTANCE: CoreBridge? = null

        fun getInstance(): CoreBridge {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CoreBridge().also { INSTANCE = it }
            }
        }

    }

    external fun getStaticKey(): ByteArray
}
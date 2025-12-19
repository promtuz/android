package com.promtuz.core

import android.content.Context
import com.promtuz.chat.utils.serialization.AppCbor
import com.promtuz.core.events.InternalEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray

object API {
    init {
        System.loadLibrary("core")
    }

    external fun initApi(context: Context)
    external fun connect(context: Context, ipk: ByteArray, isk: ByteArray)

    //=||=||=||=||=||==|  EVENTS  |==||=||=||=||=||=//

    external fun pollEvent(): ByteArray?


    val eventsFlow = callbackFlow {
        while (true) {
            val bytes = pollEvent()
            if (bytes != null) {
                // FIXME: It's temp fix
                if (bytes[0] == 0xA1.toByte()) {
                    bytes[0] = 0x82.toByte()
                }

                try {
                    @OptIn(ExperimentalSerializationApi::class)
                    trySend(AppCbor.instance.decodeFromByteArray<InternalEvent>(bytes))
                } catch (e: Exception) {
                    println("INTERNAL EVENT DESER FAIL : $e")
                }
            } else {
                delay(16) // event bus polling interval
            }
        }
    }

    //=||=||=||=||=||==| IDENTITY |==||=||=||=||=||=//

    external fun identityInit()
    external fun identityDestroy()
}
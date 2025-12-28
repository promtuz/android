package com.promtuz.core

import android.content.Context
import com.promtuz.chat.presentation.state.ConnectionState
import com.promtuz.chat.utils.serialization.AppCbor
import com.promtuz.core.events.EventCallback
import com.promtuz.core.events.InternalEvent
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import timber.log.Timber
import java.net.InetAddress

object API {
    init {
        System.loadLibrary("core")
        Timber.tag("API").d("LOADED LIBCORE");

        registerCallback(object : EventCallback {
            override fun onEvent(bytes: ByteArray) {
                if (bytes[0] == 0xA1.toByte()) bytes[0] = 0x82.toByte()

                try {
                    @OptIn(ExperimentalSerializationApi::class)
                    _eventsFlow.tryEmit(AppCbor.instance.decodeFromByteArray<InternalEvent>(bytes))
                } catch (e: Exception) {
                    Timber.tag("API").e(e, "INTERNAL EVENT DESER FAIL");
                }
            }
        })
    }

    external fun initApi(context: Context)
    external fun shouldLaunchApp(): Boolean

    //=||=||=||=||=||==|  MISC.  |==||=||=||=||=||=//

    external fun getPublicAddr(): Deferred<InetAddress?>


    //=||=||=||=||=||==|  STATS  |==||=||=||=||=||=//

    // Returns current connection state
    val connectionState: ConnectionState
        get() = ConnectionState.fromInt(getInternalConnectionState())

    private external fun getInternalConnectionState(): Int

    external fun getNetworkStats(): ByteArray

    //=||=||=||=||=||=| CONNECTION |=||=||=||=||=||=//

    external fun connect(context: Context)

    //=||=||=||=||=||==|  EVENTS  |==||=||=||=||=||=//

    private val _eventsFlow = MutableSharedFlow<InternalEvent>(
        replay = 0,
        extraBufferCapacity = 64, // Buffer for burst events
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val eventsFlow: SharedFlow<InternalEvent> = _eventsFlow.asSharedFlow()

    private external fun registerCallback(callback: EventCallback)


    //=||=||=||=||=||==| IDENTITY |==||=||=||=||=||=//

    external fun identityInit()
    external fun identityDestroy()


    //=||=||=||=||=||==| WELCOME! |==||=||=||=||=||=//

    external fun welcome(name: String): Boolean
}
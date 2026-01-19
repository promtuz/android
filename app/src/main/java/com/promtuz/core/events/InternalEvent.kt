package com.promtuz.core.events

// FIXME: shouldn't be importing presentation state in core events
import com.promtuz.chat.presentation.state.ConnectionState

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// @formatter:off

@Serializable
sealed class IdentityEvent {
    @SerialName("AddMe")
    @Serializable class AddMe(val ipk: ByteArray, val name: String) : IdentityEvent()
}

object InternalEvents {
    typealias ConnectionEv = ConnectionState
    typealias IdentityEv   = IdentityEvent
}

@Serializable
sealed class InternalEvent {
    @SerialName("Connection")
    @Serializable data class Connection(val state: ConnectionState) : InternalEvent()

    @SerialName("Identity")
    @Serializable data class Identity(val event: IdentityEvent) : InternalEvent()
}

fun interface EventCallback {
    fun onEvent(bytes: ByteArray)
}
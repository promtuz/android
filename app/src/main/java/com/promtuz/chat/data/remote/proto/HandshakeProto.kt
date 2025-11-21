package com.promtuz.chat.data.remote.proto

import com.promtuz.chat.data.remote.dto.Bytes
import kotlinx.serialization.Serializable

@Serializable
sealed class HandshakeProto {
    @Serializable
    data class ClientHello(
        val ipk: Bytes, val epk: Bytes
    ) : HandshakeProto()

    @Serializable
    data class ServerChallenge(
        val epk: Bytes, val ct: Bytes
    ) : HandshakeProto()

    @Serializable
    data class ClientProof(
        val proof: Bytes,
    ) : HandshakeProto()

    @Serializable
    data class ServerAccept(
        val timestamp: ULong
    ) : HandshakeProto()

    @Serializable
    data class ServerReject(
        val reason: String
    ) : HandshakeProto()
}

@Serializable
data class HandshakeEnvelope(
    val ClientHello: HandshakeProto.ClientHello? = null,
    val ServerChallenge: HandshakeProto.ServerChallenge? = null,
    val ClientProof: HandshakeProto.ClientProof? = null,
    val ServerAccept: HandshakeProto.ServerAccept? = null,
    val ServerReject: HandshakeProto.ServerReject? = null,
)

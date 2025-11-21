package com.promtuz.chat.data.remote.proto

import com.promtuz.chat.data.remote.dto.Bytes
import com.promtuz.chat.data.remote.dto.bytes
import com.promtuz.chat.utils.serialization.CborEnvelope
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.serialization.Serializable
import timber.log.Timber

fun bytes(n: Byte) = byteArrayOf(n)

@Serializable
sealed class HandshakeProto : CborEnvelope {
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

    fun toBytes() = when (this) {
        is ClientHello -> bytes(0x01) + ipk.bytes + epk.bytes
        is ServerChallenge -> bytes(0x02) + epk.bytes + ct.bytes
        is ClientProof -> bytes(0x03) + proof.bytes
        is ServerAccept -> bytes(0x04) + timestamp.toBytesLE()
        is ServerReject -> bytes(0x05) + reason.encodeToByteString().toByteArray()
    }

    companion object {
        private val log = Timber.tag("HandshakeProto")

        fun fromBytes(buf: ByteArray, throws: Boolean = true): HandshakeProto =
            fromBytes(buf).also { throws } ?: error("Invalid Handshake Message")

        fun fromBytes(buf: ByteArray): HandshakeProto? {
            val tag = buf[0]
            var pos = 1

            try {
                return when (tag) {
                    0x01.toByte() -> {
                        val ipk = buf.copyOfRange(pos, pos + 32)
                        pos += 32
                        val epk = buf.copyOfRange(pos, pos + 32)

                        ClientHello(ipk.bytes(), epk.bytes())
                    }

                    0x02.toByte() -> {
                        val epk = buf.copyOfRange(pos, pos + 32)
                        pos += 32
                        val ct = buf.copyOfRange(pos, pos + 32)

                        ServerChallenge(epk.bytes(), ct.bytes())
                    }

                    0x03.toByte() -> {
                        val proof = buf.copyOfRange(pos, pos + 16)
                        ClientProof(proof.bytes())
                    }

                    0x04.toByte() -> {
                        val tsBytes = buf.copyOfRange(pos, pos + 8)
                        val ts = java.nio.ByteBuffer.wrap(tsBytes).long.toULong()
                        ServerAccept(ts)
                    }

                    0x05.toByte() -> {
                        val len = buf[pos].toInt()
                        pos++
                        val reason = buf.copyOfRange(pos, pos + len).decodeToString()
                        ServerReject(reason)
                    }

                    else -> null
                }
            } catch (e: Exception) {
                log.e(e, "Decode Error:")
                return null
            }
        }
    }
}

fun ULong.toBytesLE(): ByteArray {
    val v = this.toLong()
    return ByteArray(8) { i -> (v ushr (i * 8)).toByte() }
}

fun HandshakeProto.expectChallenge(): HandshakeProto.ServerChallenge {
    return this as? HandshakeProto.ServerChallenge
        ?: error("Expected ServerChallenge")
}
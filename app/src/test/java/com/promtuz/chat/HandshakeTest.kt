package com.promtuz.chat

import com.promtuz.chat.data.remote.dto.bytes
import com.promtuz.chat.data.remote.proto.HandshakeProto
import com.promtuz.chat.utils.serialization.toCbor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class HandshakeTest {
    private val emptyBytes = ByteArray(32).bytes()
    private val filledBytes = ByteArray(32).apply { fill(255.toByte()) }.bytes()

    companion object {
        private const val F = 255.toByte()
    }

    private val helloBytes = byteArrayOf(1) + emptyBytes.bytes + filledBytes.bytes

    @Test
    fun testClientHelloEncode() {
        val hello = HandshakeProto.ClientHello(emptyBytes, filledBytes)

        assertContentEquals(
            "a16b436c69656e7448656c6c6fa26369706b582000000000000000000000000000000000000000000000000000000000000000006365706b5820ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".hexToByteArray(),
            hello.toCbor()
        )
    }

    @Test
    fun testClientHelloDecode() {
        val hello = HandshakeProto.ClientHello(emptyBytes, filledBytes)

        assertEquals(hello, HandshakeProto.fromBytes(helloBytes))
    }
}
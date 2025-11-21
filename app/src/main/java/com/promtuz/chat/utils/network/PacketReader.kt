package com.promtuz.chat.utils.network

import java.io.InputStream

class PacketReader(private val input: InputStream) {

    suspend fun readPacket(): ByteArray {
        val len = readU32()
        return readExact(len)
    }

    private suspend fun readU32(): Int {
        val b = ByteArray(4)
        readExactInto(b)
        return ((b[0].toInt() and 0xFF) shl 24) or
                ((b[1].toInt() and 0xFF) shl 16) or
                ((b[2].toInt() and 0xFF) shl 8) or
                ((b[3].toInt() and 0xFF))
    }

    private suspend fun readExact(size: Int): ByteArray {
        val buf = ByteArray(size)
        readExactInto(buf)
        return buf
    }

    private suspend fun readExactInto(buf: ByteArray) {
        var pos = 0
        while (pos < buf.size) {
            val r = input.read(buf, pos, buf.size - pos)

            if (r == -1) {
                throw IllegalStateException("Stream closed mid-packet")
            }

            if (r == 0) {
                // QUIC/Kwik: zero bytes = "not ready yet"
                // small suspension avoids hot looping
                kotlinx.coroutines.delay(1)
                continue
            }

            pos += r
        }
    }
}

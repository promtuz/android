package com.promtuz.chat

import com.promtuz.chat.data.remote.dto.ClientResponseDto
import com.promtuz.chat.data.remote.dto.ResolvedRelays
import com.promtuz.chat.data.remote.realtime.cborDecode
import com.promtuz.chat.utils.serialization.AppCbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class CBORTest {
    private val emptyRelaysResponseHex = "a16947657452656c617973a16672656c61797380"

    @Test
    fun testToEmptyRelays() {
        val emptyRelays = ClientResponseDto(ResolvedRelays(emptyList()))
        val emptyRelaysCbor = AppCbor.instance.encodeToByteArray(emptyRelays)

        assertContentEquals(
            emptyRelaysResponseHex.hexToByteArray(),
            emptyRelaysCbor
        )
    }

    @Test
    fun testFromEmptyRelays() {
        val emptyRelaysCbor = emptyRelaysResponseHex.hexToByteArray()
        val emptyRelays = cborDecode<ClientResponseDto>(emptyRelaysCbor)

        assertEquals(
            ClientResponseDto(ResolvedRelays(emptyList())),
            emptyRelays
        )
    }
}
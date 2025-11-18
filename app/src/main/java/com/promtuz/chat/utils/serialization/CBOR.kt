package com.promtuz.chat.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor

@OptIn(ExperimentalSerializationApi::class)
object AppCbor {
    val instance: Cbor = Cbor {
        ignoreUnknownKeys = true
        encodeDefaults = true
        useDefiniteLengthEncoding = true

        preferCborLabelsOverNames
    }
}

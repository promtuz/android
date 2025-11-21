package com.promtuz.chat.utils.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.serializer

@OptIn(ExperimentalSerializationApi::class)
object AppCbor {
    val instance: Cbor = Cbor {
        ignoreUnknownKeys = true
        encodeDefaults = true
        useDefiniteLengthEncoding = true
        alwaysUseByteString = true
        preferCborLabelsOverNames
    }
}

//@Serializable
interface CborEnvelope

@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
inline fun <reified T : Any> T.toCbor(): ByteArray {
    val k = this::class.simpleName ?: error("no name")
    val m = mapOf(k to this)
    return AppCbor.instance.encodeToByteArray(
        MapSerializer(String.serializer(), serializer<T>()),
        m
    )
}

package com.promtuz.chat.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString

@JvmInline
@Serializable
@OptIn(ExperimentalSerializationApi::class)
value class Bytes(@ByteString val bytes: ByteArray)

fun ByteArray.bytes(): Bytes = Bytes(this)

@Serializable
enum class CaptchaReason {
    UnregisteredPublicKey,
    SuspiciousActivity
}

@Serializable
data class CaptchaRequest(
    val reason: CaptchaReason
)

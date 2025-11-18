package com.promtuz.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed interface ClientResponse

@Serializable
@SerialName("GetRelays")
data class ResolvedRelays(
    val relays: List<RelayDescriptor>
) : ClientResponse

@Serializable
data class ClientResponseDto(
    @SerialName("GetRelays")
    val content: ResolvedRelays
)


@Serializable
data class RelayDescriptor(
    val id: String,
    val addr: String
)
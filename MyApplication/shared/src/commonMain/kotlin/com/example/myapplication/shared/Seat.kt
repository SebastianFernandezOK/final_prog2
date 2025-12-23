package com.example.myapplication.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeatsResponse(
    @SerialName("eventoId")
    val eventId: Int,

    @SerialName("asientos")
    val seats: List<Seat>
)

@Serializable
data class Seat(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("fila")
    val row: Int,

    @SerialName("columna")
    val column: Int,

    @SerialName("estado")
    val status: String
)

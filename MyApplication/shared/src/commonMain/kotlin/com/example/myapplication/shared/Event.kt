package com.example.myapplication.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    @SerialName("id")
    val id: Int,

    @SerialName("titulo")
    val name: String,

    @SerialName("resumen")
    val summary: String,

    @SerialName("descripcion")
    val description: String,

    @SerialName("fecha")
    val date: String,

    @SerialName("imagen")
    val imageUrl: String? = null,

    @SerialName("precioEntrada")
    val ticketPrice: Double,

    @SerialName("eventoTipo")
    val eventType: EventType? = null
)

@Serializable
data class EventType(
    @SerialName("nombre")
    val name: String,

    @SerialName("descripcion")
    val description: String
)

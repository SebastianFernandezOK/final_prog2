package com.example.myapplication.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventDetail(
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

    @SerialName("direccion")
    val address: String,

    @SerialName("imagen")
    val imageUrl: String? = null,

    @SerialName("filaAsientos")
    val seatRows: Int,

    @SerialName("columnAsientos")
    val seatColumns: Int,

    @SerialName("precioEntrada")
    val ticketPrice: Double,

    @SerialName("eventoTipo")
    val eventType: EventType? = null,

    @SerialName("integrantes")
    val members: List<Integrante> = emptyList()
)

@Serializable
data class Integrante(
    @SerialName("nombre")
    val firstName: String,

    @SerialName("apellido")
    val lastName: String,

    @SerialName("identificacion")
    val identification: String
)

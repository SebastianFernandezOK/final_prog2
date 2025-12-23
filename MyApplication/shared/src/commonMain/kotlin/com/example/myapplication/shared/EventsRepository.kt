package com.example.myapplication.shared

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

// Data classes
@Serializable
data class TokenRequest(val secret: String)

@Serializable
data class TokenResponse(@SerialName("token") val token: String)

@Serializable
data class SeatIdentifier(val fila: Int, val columna: Int)

@Serializable
data class BlockSeatRequest(val eventoId: Int, val asientos: List<SeatIdentifier>)

@Serializable
data class SoldSeatInfo(val fila: Int, val columna: Int, val persona: String)

@Serializable
data class SellSeatRequest(val eventoId: Int, val fecha: String, val precioVenta: Double, val asientos: List<SoldSeatInfo>)

@Serializable
data class Sale(
    val ventaId: Int, val eventoId: Int, val fechaVenta: String, val resultado: Boolean,
    val descripcion: String, val precioVenta: Double, val cantidadAsientos: Int
)

object EventsRepository {

    // IP CONFIRMADA QUE FUNCIONA
    private const val BASE_URL = "http://192.168.100.3:8081"
    private const val CLIENT_SECRET = "mi-secret-super-seguro-2025"
    private var currentToken: String? = null

    private val client = HttpClient(OkHttp) {
        engine {
            config {
                retryOnConnectionFailure(true)
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
            }
        }
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private suspend fun fetchNewToken(): String? {
        println("EventsRepository: Solicitando token a $BASE_URL...")
        return try {
            val response = client.post("$BASE_URL/api/auth/token") {
                contentType(ContentType.Application.Json)
                setBody(TokenRequest(secret = CLIENT_SECRET))
            }

            if (response.status.isSuccess()) {
                val tokenResponse: TokenResponse = response.body()
                println("EventsRepository: Token obtenido correctamente.")
                tokenResponse.token
            } else {
                println("EventsRepository: Error obteniendo token (${response.status}): ${response.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            println("EventsRepository: Excepción al obtener token: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Interceptor Manual de Auth (Restaurado)
    private suspend fun <T> authenticatedRequest(
        block: suspend (token: String?) -> HttpResponse,
        transform: suspend (HttpResponse) -> T
    ): T {
        // 1. Obtener token si no tenemos uno
        if (currentToken == null) {
            currentToken = fetchNewToken()
        }

        // 2. Hacer la petición con el token actual
        var response: HttpResponse
        try {
            response = block(currentToken)
        } catch (e: Exception) {
             println("EventsRepository: Error en petición: ${e.message}")
             throw e
        }

        // 3. Si falla por autorización (401/403), intentar refrescar el token una vez
        if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
            println("EventsRepository: Auth falló (${response.status}), intentando refrescar token...")
            val newToken = fetchNewToken()
            if (newToken != null) {
                currentToken = newToken
                response = block(currentToken)
            }
        }

        // 4. Verificar resultado final
        if (response.status.isSuccess()) {
            return transform(response)
        } else {
            val errorBody = response.bodyAsText()
            println("EventsRepository: Error Final API ${response.status}: $errorBody")
            throw Exception("API Error ${response.status}: $errorBody")
        }
    }

    private fun cacheBustUrl(path: String): String {
        return "$BASE_URL$path?_=${Clock.System.now().toEpochMilliseconds()}"
    }

    // --- Métodos Públicos ---

    suspend fun getEvents(): List<Event> {
        return authenticatedRequest(
            block = { token ->
                client.get(cacheBustUrl("/api/db/events/summary")) {
                    token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }
            },
            transform = { it.body() }
        )
    }

    suspend fun getEventDetail(eventId: Int): EventDetail {
        return authenticatedRequest(
            block = { token ->
                client.get(cacheBustUrl("/api/db/events/$eventId")) {
                    token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }
            },
            transform = { it.body() }
        )
    }

    suspend fun getSeatsForEvent(eventId: Int): List<Seat> {
        return authenticatedRequest(
            block = { token ->
                client.get(cacheBustUrl("/api/db/events/$eventId/seats")) {
                    token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }
            },
            transform = { 
                val response: SeatsResponse = it.body() 
                response.seats
            }
        )
    }

    suspend fun blockSeat(eventId: Int, seat: Seat) {
        authenticatedRequest(
            block = { token ->
                client.post("$BASE_URL/api/db/block-seats") {
                    token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    contentType(ContentType.Application.Json)
                    setBody(BlockSeatRequest(eventId, listOf(SeatIdentifier(seat.row, seat.column))))
                }
            },
            transform = { /* Unit */ }
        )
    }

    suspend fun sellSeat(eventId: Int, price: Double, seatRow: Int, seatCol: Int, personName: String) {
        authenticatedRequest(
            block = { token ->
                client.post("${BASE_URL}/api/db/sale-seats") {
                    token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                    contentType(ContentType.Application.Json)
                    setBody(SellSeatRequest(eventId, Clock.System.now().toString(), price, listOf(SoldSeatInfo(seatRow, seatCol, personName))))
                }
            },
            transform = { /* Unit */ }
        )
    }

    suspend fun getSales(): List<Sale> {
        return authenticatedRequest(
            block = { token ->
                client.get(cacheBustUrl("/api/db/sales")) {
                    token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }
            },
            transform = { it.body() }
        )
    }

    suspend fun getSalesForEvent(eventId: Int): List<Sale> {
        return authenticatedRequest(
            block = { token ->
                client.get(cacheBustUrl("/api/db/sales/event/$eventId")) {
                    token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                }
            },
            transform = { it.body() }
        )
    }
}

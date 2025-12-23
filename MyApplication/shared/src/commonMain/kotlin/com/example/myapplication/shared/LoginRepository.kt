package com.example.myapplication.shared

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val identifier: String, val password: String)

@Serializable
data class LoginResponse(val token: String)

class LoginRepository {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun login(loginRequest: LoginRequest): LoginResponse {
        // TODO: Replace with your actual backend URL
        val url = "http://10.0.2.2:8081/api/auth/login"
        return client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(loginRequest)
        }.body()
    }
}

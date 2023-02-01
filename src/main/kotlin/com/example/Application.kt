package com.example

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import com.example.plugins.*
import com.example.routes.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }
    configureTemplating()
    configureRouting()
}

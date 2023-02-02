package com.example.routes

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureIndexRouting() {
    routing {
        get("/") {
            call.respondText("Hello Klutch!")
        }
    }
}

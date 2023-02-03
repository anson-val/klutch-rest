package com.example.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

fun Application.configureLogoutRouting() {
    routing {
        route("/logout") {
            post {
                call.sessions.clear("user_session")
                call.respond(mapOf("OK" to true))
            }
        }
    }
}
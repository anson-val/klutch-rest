package com.example.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

import org.jetbrains.exposed.sql.transactions.transaction

import com.example.classes.PasswordHasher
import com.example.classes.UserLoginDTO
import com.example.classes.UserSession
import com.example.model.Users
import io.ktor.server.plugins.*
import io.ktor.server.sessions.*
import org.jetbrains.exposed.sql.select

fun Application.configureLoginRouting() {
    routing {
        route("/login") {
            post {
                val userLoginData = call.receive<UserLoginDTO>()
                var userId: Int? = null
                var userAuthority: Int? = null

                transaction {
                    val userDataVerified = Users.select { Users.email.eq(userLoginData.email) }.firstOrNull()
                        ?: throw BadRequestException("Email not found.")

                    if (!PasswordHasher.validatePassword(
                            userDataVerified[Users.password],
                            userLoginData.password.toCharArray()
                        )
                    ) {
                        throw BadRequestException("Incorrect password.")
                    }

                    userId = userDataVerified[Users.id]
                    userAuthority = userDataVerified[Users.authority]
                }

                if (userId == null || userAuthority == null) throw BadRequestException("Authentication Error.")

                call.sessions.set("user_session", UserSession(userId.toString(), userAuthority.toString(), 1))
                call.respond(mapOf("OK" to true))
            }
        }
    }
}
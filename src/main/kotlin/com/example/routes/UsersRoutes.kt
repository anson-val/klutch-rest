package com.example.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert

import com.example.classes.PasswordHasher
import com.example.classes.UserRegisterDTO
import com.example.model.Users

fun Application.configureUsersRouting() {
    routing {
        route("/users") {
            post {
                val userData = call.receive<UserRegisterDTO>()
                var userId: Int? = null

                transaction {
                    userId = Users.insert {
                        it[username] = userData.username
                        it[password] = PasswordHasher.createHash(userData.password.toCharArray())
                        it[firstName] = userData.firstName
                        it[lastName] = userData.lastName
                        it[email] = userData.email
                        it[authority] = 1
                    } get Users.id
                }

                call.respond(mapOf("User ID" to userId))
            }
        }
    }
}
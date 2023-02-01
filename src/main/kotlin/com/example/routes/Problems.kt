package com.example.routes

import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import java.util.*

import com.example.classes.*
import io.ktor.server.request.*

fun Application.configureProblemsRouting() {
    routing {
        route("problems") {
            get {
                val problems = problemStorage.map {
                    mapOf(
                        "id" to it.id,
                        "title" to it.title,
                        "description" to it.description
                    )
                }

                call.respond(
                    mapOf(
                        "data" to problems
                    ))
            }

            post {
                val newProblem = call.receive<Problem>()
                problemStorage.add(newProblem)

                call.respond(mapOf(
                    "OK" to true
                ))
            }

            route("{id}") {
                get {
                    val requestedId = call.parameters["id"]
                    val requestedProblem = problemStorage.find { it.id == requestedId }

                    call.respond(mapOf(
                        "data" to requestedProblem
                    ))
                }

                put {
                    val requestedId = call.parameters["id"]
                    problemStorage.removeIf { it.id == requestedId }

                    val updatedProblem = call.receive<Problem>()
                    problemStorage.add(updatedProblem)

                    call.respond(mapOf(
                        "OK" to true
                    ))
                }

                delete {
                    val requestedId = call.parameters["id"]
                    problemStorage.removeIf { it.id == requestedId }

                    call.respond(mapOf(
                        "OK" to true
                    ))
                }
            }
        }
    }
}

val problemStorage: MutableList<Problem> = Collections.synchronizedList(mutableListOf())
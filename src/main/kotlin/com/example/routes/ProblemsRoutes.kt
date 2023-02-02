package com.example.routes

import io.ktor.server.routing.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.application.*
import java.util.*

import com.example.classes.*
import com.example.classes.exceptions.*

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

                if (problemStorage.any { it.id == newProblem.id }) throw IdAlreadyExistsException(newProblem.id)
                problemStorage.add(newProblem)

                call.respond(mapOf(
                    "OK" to true
                ))
            }

            route("{id}") {
                get {
                    val requestedId = call.parameters["id"]!!
                    val requestedProblem = problemStorage.find { it.id == requestedId } ?: throw NotFoundException("Problem ID $requestedId not found")

                    call.respond(mapOf(
                        "data" to requestedProblem
                    ))
                }

                put {
                    val requestedId = call.parameters["id"]
                    if (!problemStorage.removeIf { it.id == requestedId }) throw NotFoundException("Problem ID $requestedId not found")

                    val updatedProblem = call.receive<Problem>()
                    problemStorage.add(updatedProblem)

                    call.respond(mapOf(
                        "OK" to true
                    ))
                }

                delete {
                    val requestedId = call.parameters["id"]
                    if (!problemStorage.removeIf { it.id == requestedId }) throw NotFoundException("Problem ID $requestedId not found")

                    call.respond(mapOf(
                        "OK" to true
                    ))
                }
            }
        }
    }
}

val problemStorage: MutableList<Problem> = Collections.synchronizedList(mutableListOf())
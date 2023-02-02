package com.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.plugins.*
import kotlinx.serialization.json.Json
import io.ktor.server.plugins.requestvalidation.*

import com.example.plugins.*
import com.example.routes.*
import com.example.classes.*
import com.example.classes.exceptions.IdAlreadyExistsException

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }

    install(StatusPages) {
        exception<BadRequestException>{ call, cause ->
            call.respondText(text = "400: $cause", status = HttpStatusCode.BadRequest)
        }

        exception<IdAlreadyExistsException>{ call, cause ->
            call.respondText(text = "400: $cause", status = HttpStatusCode.BadRequest)
        }

        exception<RequestValidationException> { call, cause ->
            call.respondText(text = "422: ${cause.reasons.joinToString()}", status = HttpStatusCode.UnprocessableEntity )
        }

        exception<NotFoundException> { call, cause ->
            call.respondText(text = "404: $cause", status = HttpStatusCode.NotFound)
        }

        exception<UnsupportedMediaTypeException> { call, cause ->
            call.respondText(text = "415: $cause", status = HttpStatusCode.UnsupportedMediaType)
        }

        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    install(RequestValidation) {
        validate<Problem> { problem ->
            when {
                problem.testCases.sumOf { it.weight } != 1.0 -> ValidationResult.Invalid("Weights in test cases should add up to 1")
                else -> ValidationResult.Valid
            }
        }
    }

    configureTemplating()
    configureIndexRouting()
    configureProblemsRouting()
}

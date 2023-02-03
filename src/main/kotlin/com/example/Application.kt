package com.example

import kotlin.collections.joinToString
import kotlin.collections.sumOf
import kotlinx.serialization.json.Json

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

import com.example.classes.ProblemPostDTO
import com.example.classes.ProblemPutDTO
import com.example.classes.UserSession
import com.example.classes.exceptions.IdAlreadyExistsException
import com.example.model.Problems
import com.example.model.TestCases
import com.example.model.Users
import com.example.routes.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun initDatabase() {
    val config = HikariConfig("/hikari.properties")
    config.schema = "public"
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Users, Problems, TestCases)
    }
}

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
        validate<ProblemPostDTO> { problem ->
            when {
                problem.testCases.sumOf { it.weight } != 1.0 -> ValidationResult.Invalid("Weights in test cases should add up to 1")
                else -> ValidationResult.Valid
            }
        }

        validate<ProblemPutDTO> { problem ->
            when {
                problem.testCases.sumOf { it.weight } != 1.0 -> ValidationResult.Invalid("Weights in test cases should add up to 1")
                else -> ValidationResult.Valid
            }
        }
    }

    install(Authentication) {
        session<UserSession>("auth-session") {
            validate {session ->
                session
            }

            challenge {
                throw BadRequestException("Authentication Error.")
            }
        }
    }

    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 86_400 // seconds in 1 day
        }
    }

    configureIndexRouting()
    configureProblemsRouting()
    configureUsersRouting()
    configureLoginRouting()
    configureLogoutRouting()
    initDatabase()
}

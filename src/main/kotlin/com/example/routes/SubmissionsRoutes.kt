package com.example.routes

import io.ktor.server.routing.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.server.application.*

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

import com.example.classes.SubmissionPostDTO
import com.example.classes.UserSession
import com.example.classes.Submission
import com.example.model.Submissions


fun Application.configureSubmissionsRouting() {
    routing {
        route("/submissions") {
            authenticate("auth-session") {
                post {
                    val userSession = call.principal<UserSession>()
                    val userIdFromSession = userSession?.userId ?: throw BadRequestException("Authentication Error.")
                    call.sessions.set(userSession.copy(accessCount = userSession.accessCount + 1))

                    val submissionData = call.receive<SubmissionPostDTO>()
                    var submissionId: Int? = null

                    transaction {
                        submissionId = Submissions.insert {
                            it[language] = submissionData.language
                            it[code] = submissionData.code
                            it[result] = "-"
                            it[executionTimeSeconds] = -1.0
                            it[score] = -1.0
                            it[problemId] = submissionData.problemId
                            it[userId] = userIdFromSession.toInt()
                        } get Submissions.id
                    }

                    call.respond(mapOf("Submission ID" to submissionId))
                }

                route("/{id}") {
                    get {
                        val userSession = call.principal<UserSession>()
                        val userIdFromSession = userSession?.userId ?: throw BadRequestException("Authentication Error.")
                        call.sessions.set(userSession.copy(accessCount = userSession.accessCount + 1))

                        val requestedId = call.parameters["id"]?.toInt()
                            ?: throw BadRequestException("Invalid ID type.")
                        var responseData: Submission? = null

                        transaction {
                            val requestedSubmission = Submissions.select {
                                Submissions.id.eq(requestedId)
                            }.first()
                            if (requestedSubmission[Submissions.userId] != userIdFromSession.toInt()) {
                                throw BadRequestException("Authentication Error.")
                            }

                            responseData = Submission(
                                id = requestedSubmission[Submissions.id],
                                language = requestedSubmission[Submissions.language],
                                code = requestedSubmission[Submissions.code],
                                result = requestedSubmission[Submissions.result],
                                score = requestedSubmission[Submissions.score],
                                executionTimeSeconds = requestedSubmission[Submissions.executionTimeSeconds],
                                problemId = requestedSubmission[Submissions.problemId],
                                userId = requestedSubmission[Submissions.userId]
                            )
                        }

                        call.respond(mapOf("data" to responseData))
                    }
                }
            }
        }
    }
}
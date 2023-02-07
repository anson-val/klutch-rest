package com.example.routes

import com.example.classes.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import io.ktor.server.application.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

import com.example.model.Submissions
import com.example.model.TestCases

const val SUBMISSION_NO_RESULT = "-"

fun Application.configureSubmissionsRouting() {
    routing {
        route("/submissions") {
            authenticate("auth-session") {
                post {
                    val userSession = call.principal<UserSession>()
                    val userIdFromSession = userSession?.userId ?: throw BadRequestException("Authentication Error.")
                    call.sessions.set(userSession.copy(accessCount = userSession.accessCount + 1))

                    val submissionData = call.receive<SubmissionPostDTO>()
                    var testCaseData:List<TestCaseDataToJudge>? = null
                    var submissionId: Int? = null

                    transaction {
                        submissionId = Submissions.insert {
                            it[language] = submissionData.language
                            it[code] = submissionData.code
                            it[result] = SUBMISSION_NO_RESULT
                            it[executionTimeSeconds] = -1.0
                            it[score] = -1.0
                            it[problemId] = submissionData.problemId
                            it[userId] = userIdFromSession.toInt()
                        } get Submissions.id

                        testCaseData = TestCases.select {
                            TestCases.problemId.eq(submissionData.problemId)
                        }.map {
                            TestCaseDataToJudge(
                                it[TestCases.input],
                                it[TestCases.expectedOutput],
                                it[TestCases.weight],
                                it[TestCases.timeOutSeconds]
                            )
                        }.toList()
                    }

                    val submissionIdToRedis: Int? = submissionId
                    val testCaseDataToRedis: List<TestCaseDataToJudge>? = testCaseData

                    if (submissionIdToRedis != null && testCaseDataToRedis != null) {
                        val submissionDataToRedis = SubmissionDataToJudge(
                            submissionIdToRedis,
                            submissionData.language,
                            submissionData.code,
                            testCaseDataToRedis
                        )

                        try {
                            RedisConnector.tryConnection()
                            RedisConnector.db!!.rpush(
                                submissionData.language,
                                Json.encodeToString(submissionDataToRedis)
                            )
                            RedisConnector.db!!.disconnect()
                        } catch (e: Exception) {
                            RedisConnector.db?.disconnect()
                            RedisConnector.db = null
                            println(e)
                        }
                    }
                    call.respond(mapOf("Submission ID" to submissionId))
                }

                route("/restart") {
                    authenticate("auth-session") {
                        post {
                            var unprocessedSubmissionDataList: List<SubmissionDataToJudge>? = null
                            var success = true

                            transaction {
                                unprocessedSubmissionDataList = Submissions.select {
                                    Submissions.result.eq(SUBMISSION_NO_RESULT)
                                }.map {
                                    val testCaseData = TestCases.select {
                                        TestCases.problemId.eq(it[Submissions.problemId])
                                    }.map {testCase ->
                                        TestCaseDataToJudge(
                                            testCase[TestCases.input],
                                            testCase[TestCases.expectedOutput],
                                            testCase[TestCases.weight],
                                            testCase[TestCases.timeOutSeconds]
                                        )
                                    }

                                    SubmissionDataToJudge(
                                        it[Submissions.id],
                                        it[Submissions.language],
                                        it[Submissions.code],
                                        testCaseData
                                    )
                                }.toList()
                            }

                            if (unprocessedSubmissionDataList != null) {
                                for (submissionData in unprocessedSubmissionDataList!!) {
                                    try {
                                        RedisConnector.tryConnection()
                                        RedisConnector.db!!.rpush(
                                            submissionData.language,
                                            Json.encodeToString(submissionData)
                                        )
                                    } catch (e: Exception) {
                                        RedisConnector.db?.disconnect()
                                        RedisConnector.db = null
                                        print(e)
                                        success = false
                                    }
                                }
                            }

                            call.respond(mapOf("OK" to success))
                        }
                    }
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

                    post("/restart") {
                        val userSession = call.principal<UserSession>()
                        val userIdFromSession = userSession?.userId ?: throw BadRequestException("Authentication Error.")
                        call.sessions.set(userSession.copy(accessCount = userSession.accessCount + 1))

                        val requestedId = call.parameters["id"]?.toInt()
                            ?: throw BadRequestException("Invalid ID type.")
                        var unprocessedSubmissionData: SubmissionDataToJudge? = null
                        var success = true

                        transaction {
                            val requestedSubmission = Submissions.select {
                                Submissions.id.eq(requestedId)
                            }.first()
                            if (requestedSubmission[Submissions.userId] != userIdFromSession.toInt()) {
                                throw BadRequestException("Authentication Error.")
                            }

                            val testCaseData = TestCases.select {
                                TestCases.problemId.eq(requestedSubmission[Submissions.problemId])
                            }.map {
                                TestCaseDataToJudge(
                                    it[TestCases.input],
                                    it[TestCases.expectedOutput],
                                    it[TestCases.weight],
                                    it[TestCases.timeOutSeconds]
                                )
                            }

                            unprocessedSubmissionData = SubmissionDataToJudge(
                                requestedSubmission[Submissions.id],
                                requestedSubmission[Submissions.language],
                                requestedSubmission[Submissions.code],
                                testCaseData
                            )
                        }

                        if (unprocessedSubmissionData != null) {
                            try {
                                RedisConnector.tryConnection()
                                RedisConnector.db!!.rpush(
                                    unprocessedSubmissionData!!.language,
                                    Json.encodeToString(unprocessedSubmissionData)
                                )
                            } catch (e: Exception) {
                                RedisConnector.db?.disconnect()
                                RedisConnector.db = null
                                print(e)
                                success = false
                            }
                        }

                        call.respond(mapOf("OK" to success))
                    }
                }
            }
        }
    }
}
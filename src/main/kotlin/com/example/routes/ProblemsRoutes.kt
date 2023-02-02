package com.example.routes

import io.ktor.server.routing.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.application.*

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.transactions.transaction

import com.example.classes.*
import com.example.model.Problems
import com.example.model.TestCases

fun Application.configureProblemsRouting() {
    routing {
        route("problems") {
            get {
                var problems: List<Map<String, String>>? = null

                transaction {
                    problems = Problems.selectAll().map {
                        mapOf(
                            "id" to it[Problems.id].toString(),
                            "title" to it[Problems.title],
                            "description" to it[Problems.description]
                        )
                    }
                }

                call.respond(
                    mapOf(
                        "data" to problems
                    )
                )
            }

            post {
                val newProblem = call.receive<ProblemPostDTO>()
                var newProblemId: Int? = null

                transaction {
                    newProblemId = Problems.insert {
                        it[title] = newProblem.title
                        it[description] = newProblem.description
                    } get Problems.id

                    for (testCase in newProblem.testCases) {
                        TestCases.insert {
                            it[input] = testCase.input
                            it[expectedOutput] = testCase.expectedOutput
                            it[comment] = testCase.comment
                            it[weight] = testCase.weight
                            it[timeOutSeconds] = testCase.timeOutSeconds
                            it[problemId] = newProblemId!!
                        }
                    }
                }

                call.respond(mapOf(
                    "Problem ID" to newProblemId
                ))
            }

            route("{id}") {
                get {
                    val requestedId = call.parameters["id"]?.toInt() ?:
                        throw BadRequestException("The type of Id is wrong.")
                    var responseData: Problem? = null

                    transaction {
                        val requestedProblem = Problems.select { Problems.id.eq(requestedId) }.first()
                        val requestedTestCases = TestCases.select { TestCases.problemId.eq(requestedId) }.map {
                            TestCase(
                                id = it[TestCases.id].toString(),
                                input = it[TestCases.input],
                                expectedOutput = it[TestCases.expectedOutput],
                                comment = it[TestCases.comment],
                                weight = it[TestCases.weight],
                                timeOutSeconds = it[TestCases.timeOutSeconds]
                            )
                        }.toList()

                        responseData = Problem(
                            id = requestedProblem[Problems.id].toString(),
                            title = requestedProblem[Problems.title],
                            description = requestedProblem[Problems.description],
                            testCases = requestedTestCases
                        )
                    }

                    call.respond(mapOf(
                        "data" to responseData
                    ))
                }

                put {
                    val requestedId = call.parameters["id"]?.toInt() ?:
                        throw BadRequestException("The type of Id is wrong.")

                    val updatedProblemContent = call.receive<ProblemPutDTO>()

                    transaction {
                        Problems.update({ Problems.id.eq(requestedId) }) {
                            it[title] = updatedProblemContent.title
                            it[description] = updatedProblemContent.description
                        }

                        TestCases.deleteWhere {
                            problemId.eq(requestedId)
                                .and(id.notInList(
                                    updatedProblemContent.testCases.mapNotNull { it.id?.toInt() }
                                ))
                        }

                        for (testcase in updatedProblemContent.testCases) {
                            if (testcase.id == null) {
                                TestCases.insert {
                                    it[input] = testcase.input
                                    it[expectedOutput] = testcase.expectedOutput
                                    it[comment] = testcase.comment
                                    it[weight] = testcase.weight
                                    it[timeOutSeconds] = testcase.timeOutSeconds
                                    it[problemId] = requestedId
                                }
                                continue
                            }

                            TestCases.update({ TestCases.id.eq(testcase.id.toInt()) }){
                                it[input] = testcase.input
                                it[expectedOutput] = testcase.expectedOutput
                                it[comment] = testcase.comment
                                it[weight] = testcase.weight
                                it[timeOutSeconds] = testcase.timeOutSeconds
                            }
                        }
                    }

                    call.respond(mapOf(
                        "Updated problem ID" to requestedId
                    ))
                }

                delete {
                    val requestedId = call.parameters["id"]?.toInt() ?:
                        throw BadRequestException("The type of Id is wrong.")

                    transaction {
                        TestCases.deleteWhere { problemId.eq(requestedId) }
                        Problems.deleteWhere { id.eq(requestedId) }
                    }

                    call.respond(mapOf(
                        "Deleted problem ID" to requestedId
                    ))
                }
            }
        }
    }
}

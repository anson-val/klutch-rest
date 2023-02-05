package com.example.model

import org.jetbrains.exposed.sql.Table

object Submissions: Table() {
    val id = integer("SubmissionId").autoIncrement()
    val language = varchar("Language", 255)
    val code = text("Code")
    val result = varchar("Result", 255)
    val executionTimeSeconds = double("ExecutionTime")
    val score = double("Score")
    override val primaryKey = PrimaryKey(id, name = "PK_Submissions_Id")

    val problemId = integer("ProblemId") references Problems.id
    val userId = integer("UserId") references Users.id
}
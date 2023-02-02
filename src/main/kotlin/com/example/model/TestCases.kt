package com.example.model

import org.jetbrains.exposed.sql.Table

object TestCases: Table() {
    val id = integer("TestCaseId").autoIncrement()
    val input = text("TestInput")
    val expectedOutput = text("ExpectedOutput")
    val comment = text("Comment")
    val weight = double("Weight")
    val timeOutSeconds = double("TimeOutSeconds")
    override val primaryKey = PrimaryKey(id, name = "PK_TestCases_Id")

    val problemId = integer("ProblemId") references Problems.id
}
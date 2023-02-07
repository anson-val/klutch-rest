package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class SubmissionDataToJudge(
    val id: Int,
    val language: String,
    val code: String,
    val testCases: List<TestCaseDataToJudge>
)

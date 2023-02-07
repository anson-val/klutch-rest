package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class TestCaseDataToJudge(
    val input: String,
    val expectedOutput: String,
    val weight: Double,
    val timeOutSeconds: Double
)

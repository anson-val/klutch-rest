package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class TestCasePostDTO(
    val input: String,
    val expectedOutput: String,
    val comment: String,
    val weight: Double,
    val timeOutSeconds: Double
)
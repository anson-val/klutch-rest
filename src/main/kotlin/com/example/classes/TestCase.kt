package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class TestCase(
    val id: String,
    val input: String,
    val expectedOutput: String,
    val comment: String,
    val weight: Double,
    val timeOutSeconds: Double
)
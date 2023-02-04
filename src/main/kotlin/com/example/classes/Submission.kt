package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class Submission(
    val id: Int,
    val language: String,
    val code: String,
    val executionTimeSeconds: Double,
    val result: String,
    val problemId: Int,
    val userId: Int
)

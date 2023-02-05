package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class Submission(
    val id: Int,
    val language: String,
    val code: String,
    val result: String,
    val score: Double,
    val executionTimeSeconds: Double,
    val problemId: Int,
    val userId: Int
)

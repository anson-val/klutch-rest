package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class ProblemPutDTO (
    val title: String,
    val description: String,
    val testCases: List<TestCasePutDTO>
)
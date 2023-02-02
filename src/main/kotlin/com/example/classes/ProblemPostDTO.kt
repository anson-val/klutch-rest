package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class ProblemPostDTO(
    val title: String,
    val description: String,
    val testCases: List<TestCasePostDTO>
)
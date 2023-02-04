package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class SubmissionPostDTO(
    val language: String,
    val code: String,
    val problemId: Int
)
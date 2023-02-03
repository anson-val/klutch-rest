package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class UserRegisterDTO(
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val email: String
)

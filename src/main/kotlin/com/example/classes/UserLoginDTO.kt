package com.example.classes

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginDTO(
    val email: String,
    val password: String
)

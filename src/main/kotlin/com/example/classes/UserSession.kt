package com.example.classes

import io.ktor.server.auth.*

data class UserSession(val userId: String, val authority: String, val accessCount: Int): Principal
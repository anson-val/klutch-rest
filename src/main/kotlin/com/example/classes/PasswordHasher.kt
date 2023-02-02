package com.example.classes

import de.mkammerer.argon2.Argon2Factory

object PasswordHasher {
    private val argon2 = Argon2Factory.create()

    fun createHash(password: CharArray): String {
        val hash: String

        try {
            hash = argon2.hash(10, 65536, 2, password)
        } finally {
            argon2.wipeArray(password)
        }

        return hash
    }

    fun validateHash(hash: String, password: CharArray): Boolean {
        val result: Boolean

        try {
            result = argon2.verify(hash, password)
        } finally {
            argon2.wipeArray(password)
        }

        return result
    }
}
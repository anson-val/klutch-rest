package com.example.classes

import kotlinx.serialization.*

@Serializable
data class TestCase(val input: String, val expectedOutput: String, val comment: String, val weight: Double, val timeOutSeconds: Double)
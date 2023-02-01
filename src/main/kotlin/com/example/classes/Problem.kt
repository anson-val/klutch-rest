package com.example.classes

import kotlinx.serialization.*

@Serializable
data class Problem (val id: String, val title: String, val description: String, val testCases: List<TestCase>)
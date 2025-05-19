package com.example.dedquiz

data class Question(
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val description: String = ""
)
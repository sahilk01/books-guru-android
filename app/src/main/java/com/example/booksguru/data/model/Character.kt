package com.example.booksguru.data.model

data class Character(
    val name: String,
    val mentions: Int,
    val description: String? = null
)
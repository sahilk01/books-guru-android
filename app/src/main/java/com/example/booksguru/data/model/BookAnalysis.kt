package com.example.booksguru.data.model

data class BookAnalysis(
    val book_id: Int,
    val status: String,
    val title: String? = null,
    val author: String? = null,
    val characters: List<Character> = emptyList(),
    val interactions: List<CharacterInteraction> = emptyList(),
    val preview: String? = null,
    val content_length: Int? = null,
    val message: String? = null
)
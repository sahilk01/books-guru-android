package com.example.booksguru.data.repository

import com.example.booksguru.data.model.BookAnalysis
import com.example.booksguru.data.remote.BookAnalysisService
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val bookAnalysisService: BookAnalysisService
) {
    suspend fun getBookAnalysis(bookId: Int): Response<BookAnalysis> {
        return bookAnalysisService.getBookAnalysis(bookId)
    }
    
    suspend fun getBookCharacters(bookId: Int): Response<BookAnalysis> {
        return bookAnalysisService.getBookCharacters(bookId)
    }
    
    suspend fun getBookVisualization(bookId: Int): Response<ResponseBody> {
        return bookAnalysisService.getBookVisualization(bookId)
    }
}
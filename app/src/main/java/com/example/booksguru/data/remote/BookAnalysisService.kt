package com.example.booksguru.data.remote

import com.example.booksguru.data.model.BookAnalysis
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BookAnalysisService {
    @GET("analyse-book/{bookId}")
    suspend fun getBookAnalysis(@Path("bookId") bookId: Int): Response<BookAnalysis>
    
    @GET("analyse-book/{bookId}/characters")
    suspend fun getBookCharacters(@Path("bookId") bookId: Int): Response<BookAnalysis>
    
    @GET("analyse-book/{bookId}/html-visualization")
    suspend fun getBookVisualization(@Path("bookId") bookId: Int): Response<ResponseBody>
}
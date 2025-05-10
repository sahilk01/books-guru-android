package com.example.booksguru.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksguru.data.model.BookAnalysis
import com.example.booksguru.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookAnalysisViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    
    private val _bookAnalysisState = MutableStateFlow<BookAnalysisState>(BookAnalysisState.Loading)
    val bookAnalysisState: StateFlow<BookAnalysisState> = _bookAnalysisState
    
    private val _bookVisualizationState = MutableStateFlow<BookVisualizationState>(BookVisualizationState.Loading)
    val bookVisualizationState: StateFlow<BookVisualizationState> = _bookVisualizationState
    
    fun fetchBookCharacters(bookId: Int) {
        viewModelScope.launch {
            _bookAnalysisState.value = BookAnalysisState.Loading
            try {
                val response = bookRepository.getBookCharacters(bookId)
                if (response.isSuccessful && response.body() != null) {
                    val bookAnalysis = response.body()!!
                    _bookAnalysisState.value = BookAnalysisState.Success(bookAnalysis)
                } else {
                    _bookAnalysisState.value = BookAnalysisState.Error("Failed to load characters. Status code: ${response.code()}")
                }
            } catch (e: Exception) {
                _bookAnalysisState.value = BookAnalysisState.Error("Error: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    fun fetchBookVisualization(bookId: Int) {
        viewModelScope.launch {
            _bookVisualizationState.value = BookVisualizationState.Loading
            try {
                val response = bookRepository.getBookVisualization(bookId)
                if (response.isSuccessful && response.body() != null) {
                    // Extract HTML content as string from the ResponseBody
                    val htmlContent = response.body()!!.string()
                    _bookVisualizationState.value = BookVisualizationState.Success(htmlContent)
                } else {
                    _bookVisualizationState.value = BookVisualizationState.Error("Failed to load visualization. Status code: ${response.code()}")
                }
            } catch (e: Exception) {
                _bookVisualizationState.value = BookVisualizationState.Error("Error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    // Add this to your ViewModel
    fun prepareHtmlForMobile(htmlContent: String): String {
        // Add viewport meta tag and mobile-specific CSS
        val mobileViewport = "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">"
        val mobileStyles = """
    <style>
    body, html {
        width: 100%;
        height: 100%;
        margin: 0;
        padding: 0;
        overflow: hidden;
    }
    #visualization {
        width: 100vw !important;
        height: 100vh !important;
        touch-action: manipulation;
    }
    svg {
        width: 100% !important;
        height: 100% !important;
        overflow: visible;
    }
    </style>
    <script>
    // Function to resize the network graph
    function resizeGraph() {
        // Force redraw of D3 visualization
        if (typeof d3 !== 'undefined') {
            // For force layouts
            if (typeof simulation !== 'undefined') {
                simulation.alpha(0.3).restart();
            }
        }
    }
    
    // Adjust for mobile
    window.addEventListener('load', function() {
        resizeGraph();
        setTimeout(resizeGraph, 500); // Run again after a delay
    });
    
    // Handle orientation changes
    window.addEventListener('resize', resizeGraph);
    </script>
    """

        // Insert into HTML head
        return if (htmlContent.contains("<head>")) {
            htmlContent.replace("<head>", "<head>$mobileViewport$mobileStyles")
        } else {
            "<html><head>$mobileViewport$mobileStyles</head><body>$htmlContent</body></html>"
        }
    }
}

sealed class BookAnalysisState {
    object Loading : BookAnalysisState()
    data class Success(val bookAnalysis: BookAnalysis) : BookAnalysisState()
    data class Error(val message: String) : BookAnalysisState()
}

sealed class BookVisualizationState {
    object Loading : BookVisualizationState()
    data class Success(val htmlContent: String) : BookVisualizationState()
    data class Error(val message: String) : BookVisualizationState()
}
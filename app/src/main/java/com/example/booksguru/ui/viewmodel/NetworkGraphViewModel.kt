package com.example.booksguru.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksguru.data.model.NetworkGraph
import com.example.booksguru.data.model.NetworkNode
import com.example.booksguru.data.model.toNetworkGraph
import com.example.booksguru.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkGraphViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    
    private val _graphState = MutableStateFlow<NetworkGraphState>(NetworkGraphState.Loading)
    val graphState: StateFlow<NetworkGraphState> = _graphState
    
    // Selected node for details view
    var selectedNode by mutableStateOf<NetworkNode?>(null)
        private set
    
    fun fetchNetworkGraph(bookId: Int) {
        viewModelScope.launch {
            _graphState.value = NetworkGraphState.Loading
            try {
                val response = bookRepository.getBookCharacters(bookId)
                if (response.isSuccessful && response.body() != null) {
                    val bookAnalysis = response.body()!!
                    val networkGraph = bookAnalysis.toNetworkGraph()
                    _graphState.value = NetworkGraphState.Success(
                        networkGraph = networkGraph,
                        bookTitle = bookAnalysis.title ?: "Unknown Book",
                        bookAuthor = bookAnalysis.author ?: "Unknown Author"
                    )
                } else {
                    _graphState.value = NetworkGraphState.Error("Failed to load graph. Status code: ${response.code()}")
                }
            } catch (e: Exception) {
                _graphState.value = NetworkGraphState.Error("Error: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    fun selectNode(node: NetworkNode?) {
        selectedNode = node
    }

    var scale by mutableStateOf(1f)
        private set

    var offset by mutableStateOf(Offset.Zero)
        private set

    fun updateZoom(newScale: Float) {
        scale = newScale.coerceIn(0.1f, 4f)
    }

    fun updateOffset(newOffset: Offset) {
        offset = newOffset
    }

    fun resetView() {
        scale = 1f
        offset = Offset.Zero
    }
}

// State class for network graph
sealed class NetworkGraphState {
    object Loading : NetworkGraphState()
    data class Success(
        val networkGraph: NetworkGraph,
        val bookTitle: String,
        val bookAuthor: String
    ) : NetworkGraphState()
    data class Error(val message: String) : NetworkGraphState()
}
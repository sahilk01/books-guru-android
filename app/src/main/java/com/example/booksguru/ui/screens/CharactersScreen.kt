package com.example.booksguru.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booksguru.ui.viewmodel.BookAnalysisState
import com.example.booksguru.ui.viewmodel.BookAnalysisViewModel

@Composable
fun CharactersScreen(
    bookId: Int,
    viewModel: BookAnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.bookAnalysisState.collectAsState()
    
    LaunchedEffect(bookId) {
        viewModel.fetchBookCharacters(bookId)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when (val currentState = state) {
            is BookAnalysisState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(text = "Analysing the book... Usually takes around 60 Seconds", textAlign = TextAlign.Center)
                }
            }
            is BookAnalysisState.Success -> {
                val bookAnalysis = currentState.bookAnalysis
                
                Column(modifier = Modifier.fillMaxSize()) {
                    // Book title and author
                    bookAnalysis.title?.let { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    bookAnalysis.author?.let { author ->
                        Text(
                            text = "by $author",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Characters list
                    Text(
                        text = "Characters",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (bookAnalysis.characters.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(bookAnalysis.characters) { character ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = character.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Text(
                                            text = "Mentions: ${character.mentions}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        
                                        character.description?.let { description ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = description,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No characters found",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            is BookAnalysisState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(onClick = { viewModel.fetchBookCharacters(bookId) }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
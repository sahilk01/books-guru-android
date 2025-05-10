package com.example.booksguru.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booksguru.ui.graph.NetworkGraphCanvas
//import com.example.booksguru.ui.graph.EnhancedNetworkGraphCanvas
//import com.example.booksguru.ui.graph.NetworkGraphCanvas
import com.example.booksguru.ui.graph.NodeDetailCard
import com.example.booksguru.ui.graph.SimpleNetworkGraphCanvas
import com.example.booksguru.ui.viewmodel.NetworkGraphState
import com.example.booksguru.ui.viewmodel.NetworkGraphViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeVisualizationScreen(
    bookId: Int,
    onNavigateBack: () -> Unit,
    viewModel: NetworkGraphViewModel = hiltViewModel()
) {
    val state by viewModel.graphState.collectAsState()
    val selectedNode = viewModel.selectedNode

    // Track help text visibility
    var showHelpText by remember { mutableStateOf(true) }

    // Hide help text after a delay
    LaunchedEffect(Unit) {
        delay(5000)
        showHelpText = false
    }

    // Load graph data
    LaunchedEffect(bookId) {
        viewModel.fetchNetworkGraph(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val currentState = state) {
                        is NetworkGraphState.Success -> {
                            Text(
                                text = currentState.bookTitle,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        else -> {
                            Text(
                                text = "Character Network",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is NetworkGraphState.Loading -> {
                    LoadingComponent()
                }
                is NetworkGraphState.Success -> {
                    // Main graph visualization
                    Column(modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.primary)) {
                        // Author info
                        Text(
                            text = "by ${currentState.bookAuthor}",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )

                        // Graph canvas with enhanced controls
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            SimpleNetworkGraphCanvas(
                                graph = currentState.networkGraph,
                                onNodeSelected = { nodeId ->
                                    val node = nodeId?.let { id ->
                                        currentState.networkGraph.nodes.find { it.id == id }
                                    }
                                    viewModel.selectNode(node)
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Help text overlay
                            this@Column.AnimatedVisibility(
                                visible = showHelpText,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Pinch to zoom • Drag to pan • Tap nodes to view details",
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Node detail card
                        NodeDetailCard(
                            node = selectedNode,
                            onClose = { viewModel.selectNode(null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is NetworkGraphState.Error -> {
                    ErrorComponent(
                        message = currentState.message,
                        onRetry = { viewModel.fetchNetworkGraph(bookId) }
                    )
                }
            }
        }
    }
}


@Composable
fun LoadingComponent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Analysing the book... Usually takes around 60 Seconds", textAlign = TextAlign.Center)
    }
}

@Composable
fun ErrorComponent(
    message: String,
    onRetry: () -> Unit,
) {
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
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

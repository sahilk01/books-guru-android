package com.example.booksguru.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.booksguru.ui.utils.rememberChromeTabsHelper
import com.example.booksguru.ui.viewmodel.BookAnalysisViewModel
import com.example.booksguru.ui.viewmodel.BookVisualizationState

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VisualizationScreen(
    bookId: Int,
    viewModel: BookAnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.bookVisualizationState.collectAsState()
    val chromeTabsHelper = rememberChromeTabsHelper()
    
    LaunchedEffect(bookId) {
        viewModel.fetchBookVisualization(bookId)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when (val currentState = state) {
            is BookVisualizationState.Loading -> {
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
            is BookVisualizationState.Success -> {
//                val htmlContent = currentState.htmlContent

                // Get HTML content
                val htmlContent = currentState.htmlContent

                // Show button to launch Chrome Custom Tabs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your network visualization is ready!",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tap the button below to open it in an optimized browser view.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            chromeTabsHelper.openHtmlContent(
                                htmlContent,
                                Color.Blue.toArgb()
                            )
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("View Interactive Network")
                    }
                }

                // Optionally, automatically launch Chrome Custom Tabs
//                LaunchedEffect(htmlContent) {
//                    chromeTabsHelper.openHtmlContent(
//                        htmlContent,
//                        Color.Blue.toArgb()
//                    )
//                }

//                AndroidView(
//                    factory = { context ->
//                        WebView(context).apply {
//                            // Essential WebView settings
//                            settings.apply {
//                                javaScriptEnabled = true
//                                domStorageEnabled = true
//                                allowFileAccess = true
//                                loadWithOverviewMode = true
//                                useWideViewPort = true
//
//                                // Enable debugging
//                                WebView.setWebContentsDebuggingEnabled(true)
//
//                                // Explicitly set the MIME type and encoding
//                                defaultTextEncodingName = "utf-8"
//
//                                // Important performance settings
//                                setRenderPriority(WebSettings.RenderPriority.HIGH)
//                                cacheMode = WebSettings.LOAD_NO_CACHE
//
//                                // Fix issues with D3 rendering
//                                setLayerType(View.LAYER_TYPE_HARDWARE, null)
//                            }
//
//                            // Debugging JavaScript console
//                            webChromeClient = object : WebChromeClient() {
//                                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
//                                    Log.d("WebView", "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()}")
//                                    return true
//                                }
//                            }
//
//                            // Handle page loading and inject fixes
//                            webViewClient = object : WebViewClient() {
//                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                                    super.onPageStarted(view, url, favicon)
//                                    Log.d("WebView", "Page started loading")
//                                }
//
//                                override fun onPageFinished(view: WebView?, url: String?) {
//                                    super.onPageFinished(view, url)
//                                    Log.d("WebView", "Page finished loading")
//
//                                    // Inject JavaScript fixes after page loads
//                                    view?.evaluateJavascript("""
//                                            // Fix SVG sizing issues
//                                            try {
//                                                const svg = document.querySelector('svg');
//                                                if (svg) {
//                                                    svg.setAttribute('width', '100%');
//                                                    svg.setAttribute('height', '100%');
//                                                }
//
//                                                // Reset zoom to ensure everything is visible
//                                                document.getElementById('resetZoom').click();
//
//                                                // Force simulation to restart
//                                                if (typeof simulation !== 'undefined') {
//                                                    simulation.alphaTarget(0.3).restart();
//                                                    setTimeout(() => {
//                                                        simulation.alphaTarget(0);
//                                                    }, 2000);
//                                                }
//                                            } catch(e) {
//                                                console.error("Fix error: " + e);
//                                            }
//                                        """.trimIndent(), null)
//                                }
//                            }
//                        }
//                    },
//                    update = { webView ->
//                        // Load content with explicit MIME type and base URL
//                        webView.loadDataWithBaseURL(
//                            "https://example.com", // Base URL for relative paths
//                            htmlContent,
//                            "text/html",
//                            "UTF-8",
//                            null
//                        )
//                    },
//                    modifier = Modifier.fillMaxSize()
//                )

//                AndroidView(
//                    factory = { context ->
//                        WebView(context).apply {
//                            settings.apply {
//                                javaScriptEnabled = true  // Critical for D3.js graphs
//                                domStorageEnabled = true  // Enable DOM storage
//                                allowFileAccess = true
//                                loadWithOverviewMode = true  // Fit content to view
//                                useWideViewPort = true  // Use wide viewport
//                                builtInZoomControls = true  // Allow pinch zoom
//                                displayZoomControls = false  // Don't display zoom controls
//                                // For better mobile rendering
//                                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
//                            }
//
//                            // Properly handle JavaScript console logs for debugging
//                            webChromeClient = WebChromeClient()
//
//                            webViewClient = object : WebViewClient() {
//                                override fun onPageFinished(view: WebView?, url: String?) {
//                                    super.onPageFinished(view, url)
//                                    // Execute JavaScript to adjust the visualization for mobile
//                                    evaluateJavascript("""
//                        document.body.style.width = '100%';
//                        document.body.style.height = '100%';
//                        document.body.style.margin = '0';
//                        document.body.style.padding = '0';
//                        if (typeof resizeGraph === 'function') {
//                            resizeGraph();
//                        }
//                    """.trimIndent(), null)
//
////                                    webViewLoaded = true
//                                }
//                            }
//                        }
//                    },
//                    update = { webView ->
//                        webView.loadDataWithBaseURL(
//                            "https://example.com",  // Provide a base URL for relative paths
//                            htmlContent,
//                            "text/html",
//                            "UTF-8",
//                            null
//                        )
//                    },
//                    modifier = Modifier.fillMaxSize()
//                )
                
//                AndroidView(
//                    factory = { context ->
//                        WebView(context).apply {
//                            settings.javaScriptEnabled = true
//                            webViewClient = WebViewClient()
//                        }
//                    },
//                    update = { webView ->
//                        webView.loadDataWithBaseURL(
//                            null,
//                            htmlContent,
//                            "text/html",
//                            "UTF-8",
//                            null
//                        )
//                    },
//                    modifier = Modifier.fillMaxSize()
//                )
            }
            is BookVisualizationState.Error -> {
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
                    
                    Button(onClick = { viewModel.fetchBookVisualization(bookId) }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
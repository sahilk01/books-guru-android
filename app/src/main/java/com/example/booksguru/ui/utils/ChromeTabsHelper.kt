package com.example.booksguru.ui.utils

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.booksguru.R
import java.io.File
import java.io.FileOutputStream

/**
 * Helper class for launching Chrome Custom Tabs
 */
class ChromeTabsHelper(private val context: Context) {
    
    /**
     * Opens HTML content in Chrome Custom Tabs by saving to a temporary file
     * @param htmlContent The HTML content to display
     * @param toolbarColor The color for the toolbar
     */
    fun openHtmlContent(htmlContent: String, toolbarColor: Int) {
        try {
            // Create a temporary file to store the HTML
            val tempFile = File(context.cacheDir, "visualization_${System.currentTimeMillis()}.html")
            FileOutputStream(tempFile).use { outputStream ->
                outputStream.write(htmlContent.toByteArray())
            }
            
            // Build the Custom Tabs intent
            val customTabsIntent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(toolbarColor)
                        .build()
                )
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .build()
            
            // Launch Custom Tabs with the file URI
            val fileUri = Uri.fromFile(tempFile)
            customTabsIntent.launchUrl(context, fileUri)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback option: Open with data URL
            openHtmlDataUrl(htmlContent, toolbarColor)
        }
    }
    
    /**
     * Alternative method using data URL if file method fails
     */
    private fun openHtmlDataUrl(htmlContent: String, toolbarColor: Int) {
        try {
            val encodedHtml = Base64.encodeToString(htmlContent.toByteArray(), Base64.NO_PADDING)
            val dataUrl = "data:text/html;charset=utf-8;base64,$encodedHtml"
            
            val customTabsIntent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(toolbarColor)
                        .build()
                )
                .setShowTitle(true)
                .build()
            
            customTabsIntent.launchUrl(context, Uri.parse(dataUrl))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Composable helper to get a ChromeTabsHelper instance
 */
@Composable
fun rememberChromeTabsHelper(): ChromeTabsHelper {
    val context = LocalContext.current
    return remember { ChromeTabsHelper(context) }
}
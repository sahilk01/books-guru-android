package com.example.booksguru.ui.graph

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

/**
 * A composable wrapper that adds zoom, pan, tap, and drag interactions to its content
 */
@Composable
fun ZoomPanTapModifier(
    onZoom: (scale: Float, offset: Offset) -> Unit,
    onTap: (position: Offset) -> Unit,
    onDragNode: (id: String?, position: Offset) -> Unit,
    onDragEnd: () -> Unit,
    findNodeAt: (position: Offset) -> String?,
    modifier: Modifier = Modifier
): Modifier {
    // Split interactions across multiple pointerInput modifiers
    // This approach is more reliable than trying to combine everything
    
    // First: Handle zooming and panning gestures
    val zoomPanModifier = modifier.pointerInput(Unit) {
        detectTransformGestures { centroid, pan, zoom, _ ->
            onZoom(zoom, pan)
        }
    }
    
    // Second: Handle tap gestures
    val tapModifier = zoomPanModifier.pointerInput(Unit) {
        detectTapGestures { position ->
            onTap(position)
        }
    }
    
    // Third: Handle drag gestures
    val dragModifier = tapModifier.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { position ->
                // Determine if we're dragging a node or the canvas
                val nodeId = findNodeAt(position)
                onDragNode(nodeId, position)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                onDragNode(null, change.position + dragAmount)
            },
            onDragEnd = {
                onDragEnd()
            },
            onDragCancel = {
                onDragEnd()
            }
        )
    }
    
    return dragModifier
}
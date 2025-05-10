package com.example.booksguru.ui.graph

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Custom gesture detector for combined zoom, pan and tap interaction
 */

//suspend fun PointerInputScope.detectZoomPanTap(
//    onZoom: (zoom: Float, pan: Offset) -> Unit,
//    onPan: (pan: Offset) -> Unit,
//    onTap: (position: Offset) -> Unit,
//    onDragStart: (position: Offset) -> Unit,
//    onDrag: (position: Offset, change: Offset) -> Unit,
//    onDragEnd: () -> Unit
//) = coroutineScope {
//    awaitEachGesture {
//        val firstDown = awaitFirstDown(requireUnconsumed = false)
//
//        // Check if this is a multi-finger gesture or a single-finger gesture
//        if (currentEvent.pointerId(firstDown.id) != null) {
//            // Handle single-finger gestures (tap or drag)
//            val downPosition = firstDown.position
//            var lastPosition = downPosition
//
//            // Wait to see if this is a tap or drag
//            val moveEvent = awaitMoveOrUp()
//
//            if (moveEvent.pressed) {
//                // This is a drag
//                onDragStart(downPosition)
//
//                // Track the drag
//                do {
//                    val current = moveEvent
//                    if (current.positionChanged()) {
//                        val currentPosition = current.position
//                        val positionChange = currentPosition - lastPosition
//
//                        onDrag(currentPosition, positionChange)
//
//                        lastPosition = currentPosition
//                    }
//                } while (moveEvent.pressed)
//
//                // End the drag
//                onDragEnd()
//            } else {
//                // This was a tap
//                onTap(downPosition)
//            }
//        } else {
//            // Handle multi-finger gestures (zoom and pan)
//            var zoom = 1f
//            var pan = Offset.Zero
//
//            do {
//                val event = awaitPointerEvent()
//                val canceled = event.changes.any { it.isConsumed }
//
//                if (!canceled && event.changes.size > 1) {
//                    // Calculate new zoom
//                    val centroid = event.calculateCentroid()
//                    val zoomChange = event.calculateZoom()
//
//                    // Calculate new pan
//                    val panChange = centroid - event.calculateCentroid(useCurrent = false)
//
//                    if (zoomChange != 1f || panChange != Offset.Zero) {
//                        zoom *= zoomChange
//                        pan += panChange
//
//                        // Call the zoom callback
//                        onZoom(zoom, pan)
//
//                        // Consume the changes
//                        event.changes.forEach { it.consume() }
//                    }
//                }
//            } while (!canceled && event.changes.any { it.pressed })
//        }
//    }
//}


//suspend fun PointerInputScope.detectZoomPanTap(
//    onZoom: (zoom: Float, pan: Offset) -> Unit,
//    onPan: (pan: Offset) -> Unit,
//    onTap: (position: Offset) -> Unit,
//    onDragStart: (position: Offset) -> Unit,
//    onDrag: (position: Offset) -> Unit,
//    onDragEnd: () -> Unit
//) = coroutineScope {
//    awaitEachGesture {
//        val velocityTracker = VelocityTracker()
//        val firstDown = awaitFirstDown(requireUnconsumed = false)
//
//        velocityTracker.addPosition(firstDown.uptimeMillis, firstDown.position)
//
//        // Check if this is a multi-finger gesture or a single-finger gesture
//        if (currentEvent.pointerId(firstDown.id) != null) {
//            // Handle single-finger gestures (tap or drag)
//            val downPosition = firstDown.position
//
//            // Wait to see if this is a tap or drag
//            val moveEvent = awaitMoveOrUp()
//
//            if (moveEvent.pressed) {
//                // This is a drag
//                onDragStart(downPosition)
//
//                // Track the drag
//                do {
//                    val current = moveEvent
//                    if (current.positionChanged()) {
//                        onDrag(current.position)
//                    }
//                } while (moveEvent.pressed)
//
//                // End the drag
//                onDragEnd()
//            } else {
//                // This was a tap
//                onTap(downPosition)
//            }
//        } else {
//            // Handle multi-finger gestures (zoom and pan)
//            var zoom = 1f
//            var pan = Offset.Zero
//
//            do {
//                val event = awaitPointerEvent()
//                val canceled = event.changes.any { it.isConsumed }
//
//                if (!canceled && event.changes.size > 1) {
//                    // Calculate new zoom
//                    val centroid = event.calculateCentroid()
//                    val zoomChange = event.calculateZoom()
//
//                    // Calculate new pan
//                    val panChange = centroid - event.calculateCentroid(useCurrent = false)
//
//                    if (zoomChange != 1f || panChange != Offset.Zero) {
//                        zoom *= zoomChange
//                        pan += panChange
//
//                        // Call the zoom callback
//                        onZoom(zoom, pan)
//
//                        // Consume the changes
//                        event.changes.forEach { it.consume() }
//                    }
//                }
//            } while (!canceled && event.changes.any { it.pressed })
//        }
//    }
//}
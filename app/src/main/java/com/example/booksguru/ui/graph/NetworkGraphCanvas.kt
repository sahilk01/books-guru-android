package com.example.booksguru.ui.graph
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booksguru.data.model.NetworkGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun SimpleNetworkGraphCanvas(
    graph: NetworkGraph,
    onNodeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // State tracking
    var size by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var draggedNodeId by remember { mutableStateOf<String?>(null) }

    // For debugging
    var debugText by remember { mutableStateOf("Initializing...") }

    // Simulation setup with initial positions
    val simulation = remember(graph, size) {
        if (size.width > 0 && size.height > 0) {
            debugText = "Creating simulation for ${graph.nodes.size} nodes, ${graph.links.size} links"

            // Create simulation
            val sim = ForceDirectedGraphSimulation(
                graph = graph,
                width = size.width.toFloat(),
                height = size.height.toFloat()
            )

            // Initialize positions in the center
            graph.nodes.forEach { node ->
                node.x = size.width / 2f
                node.y = size.height / 2f
                // Add some random offset to avoid perfect overlap
                node.x += (-50..50).random().toFloat()
                node.y += (-50..50).random().toFloat()
            }

            sim
        } else {
            debugText = "Size is zero, can't create simulation"
            null
        }
    }

    // Start simulation
    LaunchedEffect(simulation) {
        debugText = "Starting simulation"
        simulation?.startSimulation()
    }

    // Clean up
    DisposableEffect(simulation) {
        onDispose {
            debugText = "Destroying simulation"
            simulation?.onDestroy()
        }
    }

    // Draw content
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onSizeChanged { newSize ->
                size = newSize
                debugText = "Size changed: $newSize"
            }
            // Gesture handling
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.1f, 5f)
                    offset += pan
                    debugText = "Zoom: $scale, Pan: $offset"
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapPosition ->
                    debugText = "Tap at $tapPosition"

                    // Convert to graph coordinates
                    val graphPosition = (tapPosition - offset) / scale

                    // Find node at tap position
                    val nodeId = simulation?.nodes?.firstOrNull { node ->
                        val distance = sqrt(
                            (node.x - graphPosition.x).pow(2) +
                                    (node.y - graphPosition.y).pow(2)
                        )

                        val nodeRadius = 20f + (node.value * 0.5f).coerceAtMost(30f)

                        distance <= nodeRadius
                    }?.id

                    debugText += " - Node: $nodeId"
                    onNodeSelected(nodeId)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startPosition ->
                        debugText = "Drag start at $startPosition"

                        // Convert to graph coordinates
                        val graphPosition = (startPosition - offset) / scale

                        // Find node at drag start position
                        draggedNodeId = simulation?.nodes?.firstOrNull { node ->
                            val distance = sqrt(
                                (node.x - graphPosition.x).pow(2) +
                                        (node.y - graphPosition.y).pow(2)
                            )

                            val nodeRadius = 20f + (node.value * 0.5f).coerceAtMost(30f)

                            distance <= nodeRadius
                        }?.id

                        debugText += " - Node: $draggedNodeId"
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        if (draggedNodeId != null) {
                            // Drag node
                            debugText = "Dragging node $draggedNodeId"

                            // Convert to graph coordinates
                            val graphPosition = (change.position - offset) / scale

                            // Update node position
                            simulation?.nodes?.find { it.id == draggedNodeId }?.apply {
                                x = graphPosition.x
                                y = graphPosition.y
                            }
                        } else {
                            // Pan canvas
                            debugText = "Panning canvas by $dragAmount"
                            offset += dragAmount
                        }
                    },
                    onDragEnd = {
                        debugText = "Drag ended"
                        draggedNodeId = null
                    },
                    onDragCancel = {
                        debugText = "Drag canceled"
                        draggedNodeId = null
                    }
                )
            }
    ) {
        // Draw debugging info
        Text(
            text = debugText,
            color = Color.Black,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp)
        )

        // Draw graph
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Skip if simulation or nodes not ready
            val nodes = simulation?.nodes ?: return@Canvas

            debugText = "Drawing ${nodes.size} nodes"

            // Apply transformations
            withTransform({
                translate(offset.x, offset.y)
                scale(scale, scale)
            }) {
                // Draw links first (behind nodes)
                for (link in graph.links) {
                    val source = nodes.find { it.id == link.source } ?: continue
                    val target = nodes.find { it.id == link.target } ?: continue

                    // Calculate link width based on value
                    val strokeWidth = 2f + (link.value * 0.2f).coerceAtMost(5f)

                    // Draw link
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(source.x, source.y),
                        end = Offset(target.x, target.y),
                        strokeWidth = strokeWidth
                    )
                }

                // Draw nodes
                for (node in nodes) {
                    // Calculate node radius based on value
                    val nodeRadius = 20f + (node.value * 0.5f).coerceAtMost(30f)

                    // Determine if node is selected or being dragged
                    val isSelected = node.id == draggedNodeId

                    // Draw node circle (with highlight if selected)
                    drawCircle(
                        color = if (isSelected) {
                            Color.Yellow
                        } else {
                            Color.Blue
                        },
                        radius = nodeRadius,
                        center = Offset(node.x, node.y)
                    )

                    // Draw node outline for better visibility
                    drawCircle(
                        color = Color.Blue,
                        radius = nodeRadius,
                        center = Offset(node.x, node.y),
                        style = Stroke(width = 2f)
                    )

                    // Draw node label
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                            setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
                        }

                        drawText(
                            node.name,
                            node.x,
                            node.y + nodeRadius + 30f,
                            paint
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//fun SimpleNetworkGraphCanvas(
//    graph: NetworkGraph,
//    onNodeSelected: (String?) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    // State tracking
//    var size by remember { mutableStateOf(IntSize.Zero) }
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//    var draggedNodeId by remember { mutableStateOf<String?>(null) }
//
//    // Simulation setup
//    val simulation = remember(graph, size) {
//        if (size.width > 0 && size.height > 0) {
//            ForceDirectedGraphSimulation(graph, size.width.toFloat(), size.height.toFloat())
//        } else null
//    }
//
//    LaunchedEffect(simulation) { simulation?.startSimulation() }
//    DisposableEffect(simulation) { onDispose { simulation?.onDestroy() } }
//
//    // Find node at position helper function
//    val findNodeAt = { position: Offset ->
//        val graphPosition = (position - offset) / scale
//        simulation?.nodes?.firstOrNull { node ->
//            val distance = (Offset(node.x, node.y) - graphPosition).getDistance()
//            val nodeRadius = min(10f, 5f + node.value * 0.2f)
//            distance <= nodeRadius * 2
//        }?.id
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .clipToBounds()
//            .background(MaterialTheme.colorScheme.background)
//            .onSizeChanged { size = it }
//            // Handle zoom/pan
//            .pointerInput(Unit) {
//                detectTransformGestures { _, pan, zoom, _ ->
//                    scale *= zoom
//                    offset += pan * scale
//                }
//            }
//            // Handle tap for node selection
//            .pointerInput(Unit) {
//                detectTapGestures { tapPosition ->
//                    onNodeSelected(findNodeAt(tapPosition))
//                }
//            }
//            // Handle node dragging
//            .pointerInput(Unit) {
//                detectDragGestures(
//                    onDragStart = { startPosition ->
//                        draggedNodeId = findNodeAt(startPosition)
//                    },
//                    onDrag = { change, dragAmount ->
//                        if (draggedNodeId != null) {
//                            // Drag a node
//                            val graphPosition = (change.position - offset) / scale
//                            simulation?.dragNode(draggedNodeId!!, graphPosition)
//                        } else {
//                            // Pan the canvas
//                            offset += dragAmount
//                        }
//                    },
//                    onDragEnd = {
//                        if (draggedNodeId != null) {
//                            simulation?.releaseNode(draggedNodeId!!)
//                            draggedNodeId = null
//                        }
//                    }
//                )
//            }
//    ) {
//        // Draw graph using Canvas
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val nodes = simulation?.nodes ?: return@Canvas
//
//            // Create node map for link rendering
//            val nodeMap = nodes.associateBy { it.id }
//
//            // Apply zoom and pan transformation
//            translate(offset.x, offset.y) {
//                scale(scale) {
//                    // Draw links
//                    for (link in graph.links) {
//                        val source = nodeMap[link.source] ?: continue
//                        val target = nodeMap[link.target] ?: continue
//
//                        drawLine(
//                            color = Color.White.copy(alpha = 0.4f),
//                            start = Offset(source.x, source.y),
//                            end = Offset(target.x, target.y),
//                            strokeWidth = 1f + link.value * 0.1f
//                        )
//                    }
//
//                    // Draw nodes
//                    for (node in nodes) {
//                        val isSelected = node.id == draggedNodeId
//                        val nodeRadius = min(10f, 5f + node.value * 0.2f)
//
//                        drawCircle(
//                            color = if (isSelected) Color.Yellow else Color.White,
//                            radius = nodeRadius * 2,
//                            center = Offset(node.x, node.y)
//                        )
//
//                        // Draw node label
//                        drawContext.canvas.nativeCanvas.apply {
//                            val textSize = 35f
//                            val paint = android.graphics.Paint().apply {
//                                color = android.graphics.Color.WHITE
//                                this.textSize = textSize
//                                textAlign = android.graphics.Paint.Align.CENTER
//                            }
//
//                            drawText(node.name, node.x, node.y + nodeRadius * 2 + 20f, paint)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun NetworkGraphCanvas(
    graph: NetworkGraph,
    onNodeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track canvas size
    var size by remember { mutableStateOf(IntSize.Zero) }

    // Track zoom and pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Track which node is being dragged
    var draggedNodeId by remember { mutableStateOf<String?>(null) }
    var isDraggingCanvas by remember { mutableStateOf(false) }

    // Remember the physics simulation
    val simulation = remember(graph, size) {
        if (size.width > 0 && size.height > 0) {
            ForceDirectedGraphSimulation(
                graph = graph,
                width = size.width.toFloat(),
                height = size.height.toFloat()
            )
        } else {
            null
        }
    }

    // Start simulation when ready
    LaunchedEffect(simulation) {
        simulation?.startSimulation()
    }

    // Clean up simulation when composable is disposed
    DisposableEffect(simulation) {
        onDispose {
            simulation?.onDestroy()
        }
    }

    // Helper function to find a node at a specific position
    val findNodeAt: (Offset) -> String? = remember {
        { position ->
            // Convert screen position to graph coordinates
            val graphPosition = (position - offset) / scale

            simulation?.nodes?.firstOrNull { node ->
                val dx = node.x - graphPosition.x
                val dy = node.y - graphPosition.y
                val distance = sqrt(dx * dx + dy * dy)

                // Calculate node radius based on value
                val nodeRadius = min(10f, 5f + node.value * 0.2f)

                // Check if position is within node
                distance <= nodeRadius * 2
            }?.id
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background)
            .onSizeChanged { size = it }
            // Use custom gesture modifier
            .let { baseModifier ->
                ZoomPanTapModifier(
                    onZoom = { zoomFactor, pan ->
                        // Update scale and offset
                        scale *= zoomFactor
                        offset += pan * scale
                    },
                    onTap = { position ->
                        // Find tapped node
                        val nodeId = findNodeAt(position)
                        onNodeSelected(nodeId)
                    },
                    onDragNode = { nodeId, position ->
                        if (nodeId != null && draggedNodeId == null) {
                            // Start dragging a node
                            draggedNodeId = nodeId
                            // Update node position
                            val graphPosition = (position - offset) / scale
                            simulation?.dragNode(nodeId, graphPosition)
                        } else if (nodeId == null && draggedNodeId != null) {
                            // Continue dragging a node
                            val graphPosition = (position - offset) / scale
                            simulation?.dragNode(draggedNodeId!!, graphPosition)
                        } else if (nodeId == null && draggedNodeId == null) {
                            // Dragging the canvas
                            if (!isDraggingCanvas) {
                                isDraggingCanvas = true
                            } else {
                                // Update canvas position
                                offset += Offset(position.x, position.y) - Offset(
//                                    position.x - dragAmount.x,
//                                    position.y - dragAmount.y
                                    position.x,
                                    position.y
                                )
                            }
                        }
                    },
                    onDragEnd = {
                        // Release dragged node
                        if (draggedNodeId != null) {
                            simulation?.releaseNode(draggedNodeId!!)
                            draggedNodeId = null
                        }
                        isDraggingCanvas = false
                    },
                    findNodeAt = findNodeAt
                )
            }
    ) {
        // Draw the graph
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Drawing code...
        }
    }
}

//@Composable
//fun EnhancedNetworkGraphCanvas(
//    graph: NetworkGraph,
//    onNodeSelected: (String?) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    // Track canvas size
//    var size by remember { mutableStateOf(IntSize.Zero) }
//
//    // Convert to density-independent pixels for better text rendering
//    val density = LocalDensity.current
//
//    val scope = rememberCoroutineScope()
//
//    // Track zoom and pan state
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//
//    // Track controls visibility
//    var controlsVisible by remember { mutableStateOf(true) }
//
//    // Auto-hide controls after delay
//    LaunchedEffect(Unit) {
//        delay(5000)
//        controlsVisible = false
//    }
//
//    // Track which node is being dragged
//    var draggedNodeId by remember { mutableStateOf<String?>(null) }
//
//    // Remember the physics simulation
//    val simulation = remember(graph, size) {
//        if (size.width > 0 && size.height > 0) {
//            ForceDirectedGraphSimulation(
//                graph = graph,
//                width = size.width.toFloat(),
//                height = size.height.toFloat()
//            )
//        } else {
//            null
//        }
//    }
//
//    // Start simulation when ready
//    LaunchedEffect(simulation) {
//        simulation?.startSimulation()
//    }
//
//    // Clean up simulation when composable is disposed
//    DisposableEffect(simulation) {
//        onDispose {
//            simulation?.onDestroy()
//        }
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .clipToBounds()
//            .background(MaterialTheme.colorScheme.background)
//            .onSizeChanged { size = it }
//            .pointerInput(Unit) {
//                // Custom gesture detection for the graph
//                detectZoomPanTap(
//                    onZoom = { zoomChange, panChange ->
//                        scale *= zoomChange
//                        offset += panChange
//                        controlsVisible = true
//
//                        // Auto-hide controls after delay
//                        scope.launch {
//                            delay(3000)
//                            controlsVisible = false
//                        }
//                    },
//                    onPan = { panChange ->
//                        offset += panChange
//                        controlsVisible = true
//
//                        // Auto-hide controls after delay
//                        scope.launch {
//                            delay(3000)
//                            controlsVisible = false
//                        }
//                    },
//                    onTap = { position ->
//                        // Convert tap position to graph coordinates
//                        val graphPosition = (position - offset) / scale
//
//                        // Find tapped node
//                        val tappedNode = simulation?.nodes?.firstOrNull { node ->
//                            val dx = node.x - graphPosition.x
//                            val dy = node.y - graphPosition.y
//                            val distance = sqrt(dx * dx + dy * dy)
//
//                            // Calculate node radius based on value
//                            val nodeRadius = min(10f, 5f + node.value * 0.2f)
//
//                            // Check if tap is within node
//                            distance <= nodeRadius * 2
//                        }
//
//                        // Update selected node
//                        onNodeSelected(tappedNode?.id)
//
//                        // Show controls
//                        controlsVisible = true
//                    },
//                    onDragStart = { position ->
//                        // Convert drag position to graph coordinates
//                        val graphPosition = (position - offset) / scale
//
//                        // Find dragged node
//                        draggedNodeId = simulation?.nodes?.firstOrNull { node ->
//                            val dx = node.x - graphPosition.x
//                            val dy = node.y - graphPosition.y
//                            val distance = sqrt(dx * dx + dy * dy)
//
//                            // Calculate node radius based on value
//                            val nodeRadius = min(10f, 5f + node.value * 0.2f)
//
//                            // Check if drag started within node
//                            distance <= nodeRadius * 2
//                        }?.id
//                    },
//                    onDrag = { position ->
//                        // Update dragged node position
//                        // Update dragged node position
//                        if (draggedNodeId != null) {
//                            val graphPosition = (position - offset) / scale
//                            simulation?.dragNode(draggedNodeId!!, graphPosition)
//                        } else {
//                            // Pan the canvas if no node is being dragged
//                            // We can't use position.positionChange() - we need to track the previous position
//                            val lastPosition = previousPosition ?: position
//                            offset += position - lastPosition
//                            previousPosition = position
//                        }
//
//                        // Show controls
//                        controlsVisible = true
//                    },
//                    onDragEnd = {
//                        // Release dragged node
//                        if (draggedNodeId != null) {
//                            simulation?.releaseNode(draggedNodeId!!)
//                            draggedNodeId = null
//                        }
//
//                        // Auto-hide controls after delay
//                        scope.launch {
//                            delay(3000)
//                            controlsVisible = false
//                        }
//                    }
//                )
//            }
//    ) {
//        // Draw the graph
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            // Skip drawing if simulation is not ready
//            val nodes = simulation?.nodes ?: return@Canvas
//
//            // ... (same drawing code as before)
//        }
//
//        // Add controls at the bottom
//        GraphControls(
//            onZoomIn = { scale *= 1.2f },
//            onZoomOut = { scale *= 0.8f },
//            onReset = { simulation?.resetSimulation() },
//            onCenter = {
//                offset = Offset.Zero
//                scale = 1f
//            },
//            modifier = Modifier.align(Alignment.BottomCenter),
//            visible = controlsVisible
//        )
//    }
//}

//@Composable
//fun NetworkGraphCanvas(
//    graph: NetworkGraph,
//    onNodeSelected: (String?) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    // Track canvas size
//    var size by remember { mutableStateOf(IntSize.Zero) }
//
//    // Convert to density-independent pixels for better text rendering
//    val density = LocalDensity.current
//
//    // Track zoom and pan state
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//
//    // Track which node is being dragged
//    var draggedNodeId by remember { mutableStateOf<String?>(null) }
//
//    // Remember the physics simulation
//    val simulation = remember(graph, size) {
//        if (size.width > 0 && size.height > 0) {
//            ForceDirectedGraphSimulation(
//                graph = graph,
//                width = size.width.toFloat(),
//                height = size.height.toFloat()
//            )
//        } else {
//            null
//        }
//    }
//
//    // Start simulation when ready
//    LaunchedEffect(simulation) {
//        simulation?.startSimulation()
//    }
//
//    // Clean up simulation when composable is disposed
//    DisposableEffect(simulation) {
//        onDispose {
//            simulation?.onDestroy()
//        }
//    }
//
//    // Get theme colors
//    val nodeColor = MaterialTheme.colorScheme.primary
//    val selectedNodeColor = MaterialTheme.colorScheme.tertiary
//    val linkColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
//    val textColor = MaterialTheme.colorScheme.onSurface
//
//    // Transformable state for zoom and pan
//    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
//        scale *= zoomChange
//        offset += panChange * scale
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .clipToBounds()
//            .background(MaterialTheme.colorScheme.background)
//            .onSizeChanged { size = it }
//            .transformable(state = transformableState)
//            .pointerInput(Unit) {
//                detectTapGestures { tapPosition ->
//                    // Convert tap position to graph coordinates
//                    val graphPosition = (tapPosition - offset) / scale
//
//                    // Find tapped node
//                    val tappedNode = simulation?.nodes?.firstOrNull { node ->
//                        val dx = node.x - graphPosition.x
//                        val dy = node.y - graphPosition.y
//                        val distance = sqrt(dx * dx + dy * dy)
//
//                        // Calculate node radius based on value
//                        val nodeRadius = min(10f, 5f + node.value * 0.2f)
//
//                        // Check if tap is within node
//                        distance <= nodeRadius * 2
//                    }
//
//                    // Update selected node
//                    onNodeSelected(tappedNode?.id)
//                }
//            }
//            .pointerInput(Unit) {
//                detectDragGestures(
//                    onDragStart = { dragStart ->
//                        // Convert drag position to graph coordinates
//                        val graphPosition = (dragStart - offset) / scale
//
//                        // Find dragged node
//                        draggedNodeId = simulation?.nodes?.firstOrNull { node ->
//                            val dx = node.x - graphPosition.x
//                            val dy = node.y - graphPosition.y
//                            val distance = sqrt(dx * dx + dy * dy)
//
//                            // Calculate node radius based on value
//                            val nodeRadius = min(10f, 5f + node.value * 0.2f)
//
//                            // Check if drag started within node
//                            distance <= nodeRadius * 2
//                        }?.id
//                    },
//                    onDrag = { change, _ ->
//                        // Update dragged node position
//                        if (draggedNodeId != null) {
//                            val graphPosition = (change.position - offset) / scale
//                            simulation?.dragNode(draggedNodeId!!, graphPosition)
//                        } else {
//                            // Pan the canvas if no node is being dragged
//                            offset += change.positionChange() * scale
//                        }
//                    },
//                    onDragEnd = {
//                        // Release dragged node
//                        if (draggedNodeId != null) {
//                            simulation?.releaseNode(draggedNodeId!!)
//                            draggedNodeId = null
//                        }
//                    },
//                    onDragCancel = {
//                        // Release dragged node
//                        if (draggedNodeId != null) {
//                            simulation?.releaseNode(draggedNodeId!!)
//                            draggedNodeId = null
//                        }
//                    }
//                )
//            }
//    ) {
//        // Draw the graph
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            // Skip drawing if simulation is not ready
//            val nodes = simulation?.nodes ?: return@Canvas
//
//            // Create node map for link rendering
//            val nodeMap = nodes.associateBy { it.id }
//
//            // Apply zoom and pan
//            translate(offset.x, offset.y) {
//                // Scale canvas
//                scale(scale, scale, Offset.Zero) {
//                    // Draw links first (behind nodes)
//                    for (link in graph.links) {
//                        val source = nodeMap[link.source] ?: continue
//                        val target = nodeMap[link.target] ?: continue
//
//                        // Calculate link width based on value
//                        val strokeWidth = 1f + (link.value * 0.1f).coerceAtMost(3f)
//
//                        // Draw link
//                        drawLine(
//                            color = linkColor,
//                            start = Offset(source.x, source.y),
//                            end = Offset(target.x, target.y),
//                            strokeWidth = strokeWidth
//                        )
//                    }
//
//                    // Draw nodes
//                    for (node in nodes) {
//                        // Calculate node radius based on value
//                        val nodeRadius = min(10f, 5f + node.value * 0.2f)
//
//                        // Determine if node is selected
//                        val isSelected = node.id == draggedNodeId
//
//                        // Draw node circle
//                        drawCircle(
//                            color = if (isSelected) selectedNodeColor else nodeColor,
//                            radius = nodeRadius * 2,
//                            center = Offset(node.x, node.y)
//                        )
//
//                        // Draw node outline
//                        drawCircle(
//                            color = Color.White,
//                            radius = nodeRadius * 2,
//                            center = Offset(node.x, node.y),
//                            style = Stroke(width = 1f)
//                        )
//
//                        // Draw node label
//                        drawContext.canvas.nativeCanvas.apply {
//                            val textSize = density.run { 12.dp.toPx() }
//                            val text = node.name
//
//                            // Create paint object for text
//                            val paint = android.graphics.Paint().apply {
//                                color = android.graphics.Color.WHITE
//                                this.textSize = textSize
//                                textAlign = android.graphics.Paint.Align.CENTER
//                                isFakeBoldText = true
//                                setShadowLayer(2f, 0f, 0f, android.graphics.Color.BLACK)
//                            }
//
//                            // Draw text
//                            drawText(
//                                text,
//                                node.x,
//                                node.y + nodeRadius * 2 + textSize,
//                                paint
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
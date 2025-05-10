package com.example.booksguru.ui.graph

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.example.booksguru.data.model.NetworkGraph
import com.example.booksguru.data.model.NetworkNode
import com.example.booksguru.data.model.NetworkLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class ForceDirectedGraphSimulation(
    private val graph: NetworkGraph,
    private val width: Float,
    private val height: Float
) {
    // Mutable internal state
    private var _nodes by mutableStateOf(graph.nodes.toList())
    
    // Accessible state
    val nodes: List<NetworkNode>
        get() = _nodes
        
    // Node map for quick lookup
    private val nodeMap = graph.nodes.associateBy { it.id }
    
    // Force parameters
    private val linkDistance = 100f
    private val linkStrength = 0.7f
    private val chargeStrength = -500f
    private val centerStrength = 0.1f
    private val decay = 0.9f
    
    // Simulation state
    private var alpha = 1f
    private var alphaMin = 0.001f
    private var alphaDecay = 1 - decay.pow(1f / 300)
    
    // Simulation job
    private var simulationJob: Job? = null
    private var coroutineScope = CoroutineScope(Dispatchers.Default)
    
    init {
        // Initialize node positions randomly
        _nodes.forEach { node ->
            node.x = (0.3 + Math.random() * 0.4).toFloat() * width
            node.y = (0.3 + Math.random() * 0.4).toFloat() * height
        }
    }
    
    fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = coroutineScope.launch {
            while (isActive && alpha > alphaMin) {
                tick()
                delay(16) // ~60fps
            }
        }
    }
    
    fun stopSimulation() {
        simulationJob?.cancel()
    }
    
    fun resetSimulation() {
        alpha = 1f
        _nodes.forEach { node ->
            node.x = (0.3 + Math.random() * 0.4).toFloat() * width
            node.y = (0.3 + Math.random() * 0.4).toFloat() * height
            node.vx = 0f
            node.vy = 0f
        }
        startSimulation()
    }
    
    private fun tick() {
        // Apply forces
        applyLinkForce()
        applyChargeForce()
        applyCenterForce()
        
        // Update positions
        _nodes = _nodes.map { it.copy() }.also { newNodes ->
            for (i in newNodes.indices) {
                val node = newNodes[i]
                
                // Update velocity with decay
                node.vx *= decay
                node.vy *= decay
                
                // Update position based on velocity
                node.x += node.vx
                node.y += node.vy
                
                // Reset velocity for next frame
                node.vx = 0f
                node.vy = 0f
            }
        }
        
        // Cool down simulation
        alpha += (alphaMin - alpha) * alphaDecay
    }
    
    private fun applyLinkForce() {
        for (link in graph.links) {
            val source = nodeMap[link.source] ?: continue
            val target = nodeMap[link.target] ?: continue
            
            // Calculate distance
            val dx = target.x - source.x
            val dy = target.y - source.y
            var distance = sqrt(dx * dx + dy * dy)
            
            // Avoid division by zero
            if (distance < 0.1f) {
                distance = 0.1f
            }
            
            // Calculate force strength based on link value
            val strength = linkStrength * (1 + link.value * 0.1f) 
            
            // Calculate force
            val forceFactor = strength * alpha * (linkDistance - distance) / distance
            
            // Apply force to nodes
            source.vx -= dx * forceFactor
            source.vy -= dy * forceFactor
            target.vx += dx * forceFactor
            target.vy += dy * forceFactor
        }
    }
    
    private fun applyChargeForce() {
        for (i in _nodes.indices) {
            for (j in i + 1 until _nodes.size) {
                val nodeA = _nodes[i]
                val nodeB = _nodes[j]
                
                // Calculate distance
                val dx = nodeB.x - nodeA.x
                val dy = nodeB.y - nodeA.y
                var distance = sqrt(dx * dx + dy * dy)
                
                // Avoid division by zero and limit maximum force at close distances
                if (distance < 1f) {
                    distance = 1f
                }
                
                // Calculate force strength based on node values
                val strengthA = abs(chargeStrength) * (1 + nodeA.value * 0.01f)
                val strengthB = abs(chargeStrength) * (1 + nodeB.value * 0.01f)
                
                // Calculate force
                val forceFactor = alpha * strengthA * strengthB / (distance * distance)
                
                // Apply force to nodes
                val forceX = dx * forceFactor / distance
                val forceY = dy * forceFactor / distance
                
                nodeA.vx -= forceX
                nodeA.vy -= forceY
                nodeB.vx += forceX
                nodeB.vy += forceY
            }
        }
    }
    
    private fun applyCenterForce() {
        for (node in _nodes) {
            // Force toward center
            val dx = width / 2 - node.x
            val dy = height / 2 - node.y
            
            // Apply force with strength proportional to distance from center
            node.vx += dx * centerStrength * alpha
            node.vy += dy * centerStrength * alpha
        }
    }
    
    fun dragNode(nodeId: String, position: Offset) {
        val node = _nodes.find { it.id == nodeId } ?: return
        
        // Update the fixed position
        node.fx = position.x
        node.fy = position.y
        
        // Move the node to that position
        node.x = position.x
        node.y = position.y
        
        // Reset velocity
        node.vx = 0f
        node.vy = 0f
    }
    
    fun releaseNode(nodeId: String) {
        val node = _nodes.find { it.id == nodeId } ?: return
        
        // Release the fixed position
        node.fx = null
        node.fy = null
    }
    
    fun onDestroy() {
        coroutineScope.cancel()
    }
}
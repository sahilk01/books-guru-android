package com.example.booksguru.data.model

data class NetworkNode(
    val id: String,
    val name: String,
    val value: Int,
    val description: String,
    var x: Float = 0f, // Position for rendering
    var y: Float = 0f,
    var vx: Float = 0f, // Velocity for simulation
    var vy: Float = 0f,
    var fx: Float? = null, // Fixed position (when dragging)
    var fy: Float? = null
)

data class NetworkLink(
    val source: String,
    val target: String,
    val value: Int
)

data class NetworkGraph(
    val nodes: List<NetworkNode>,
    val links: List<NetworkLink>
)

fun BookAnalysis.toNetworkGraph(): NetworkGraph {
    val nodes = characters.map { character ->
        NetworkNode(
            id = character.name,
            name = character.name,
            value = character.mentions,
            description = character.description ?: "Mentioned ${character.mentions} times in the book."
        )
    }

    val links = interactions.map { interaction ->
        NetworkLink(
            source = interaction.character1,
            target = interaction.character2,
            value = interaction.interaction_count
        )
    }

    return NetworkGraph(nodes, links)
}
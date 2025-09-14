package hu.akos.hollo.szabo.model

data class GraphEdge<E>(
    val sourceNodeIndex: Int,
    val targetNodeIndex: Int,
    val value: E
)
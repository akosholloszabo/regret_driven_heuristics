package hu.akos.hollo.szabo.utils

data class GraphEdge<E>(
    val sourceNodeIndex: Int,
    val targetNodeIndex: Int,
    val value: E
)
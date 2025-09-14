package hu.akos.hollo.szabo.heuristics

import hu.akos.hollo.szabo.model.GraphEdge
import hu.akos.hollo.szabo.model.dimensions
import hu.akos.hollo.szabo.model.mapEachEntryIndexed


fun oneShotEdgeBuilder(regret: Array<DoubleArray>): IntArray {
    val sequentialRepresentation = IntArray(regret.dimensions.first) { Int.MIN_VALUE }
    val inverseSequentialRepresentation = IntArray(regret.dimensions.first) { Int.MIN_VALUE }
    val sequencesFromSource = IntArray(regret.dimensions.first) { Int.MIN_VALUE }
    val sequencesToTarget = IntArray(regret.dimensions.first) { Int.MIN_VALUE }
    var selectedEdges = 0

    var graphEdges =
        regret
            .mapEachEntryIndexed { columnIndex, rowIndex, value ->
                GraphEdge(
                    sourceNodeIndex = columnIndex,
                    targetNodeIndex = rowIndex,
                    value = value
                )
            }
            .map {
                it
                    .filter { it.sourceNodeIndex != it.targetNodeIndex }
                    .toList()
            }
            .toList()

    while (selectedEdges != regret.dimensions.first - 1) {

        graphEdges.asSequence()
            .map {
                it.asSequence()
                    .filter { it.sourceNodeIndex != it.targetNodeIndex }
                    .minBy { it.value }
            }
            .sortedBy { it.value }
            .filter { edge -> sequentialRepresentation[edge.sourceNodeIndex] == Int.MIN_VALUE }
            .filter { edge -> inverseSequentialRepresentation[edge.targetNodeIndex] == Int.MIN_VALUE }
            .filter { edge -> sequentialRepresentation[edge.targetNodeIndex] != edge.sourceNodeIndex }
            .filter { edge -> sequencesFromSource[edge.targetNodeIndex] != edge.sourceNodeIndex }
            .filter { edge -> sequencesToTarget[edge.sourceNodeIndex] != edge.targetNodeIndex }
            .forEach { regretEdge ->
                val matchingOnSource = sequencesToTarget[regretEdge.sourceNodeIndex]
                val matchingOnTarget = sequencesFromSource[regretEdge.targetNodeIndex]
                val newSequence = when {
                    matchingOnSource == Int.MIN_VALUE && matchingOnTarget == Int.MIN_VALUE ->
                        GraphEdge(regretEdge.sourceNodeIndex, regretEdge.targetNodeIndex, Unit)

                    matchingOnSource != Int.MIN_VALUE && matchingOnTarget == Int.MIN_VALUE ->
                        GraphEdge(matchingOnSource, regretEdge.targetNodeIndex, Unit)

                    matchingOnSource == Int.MIN_VALUE && matchingOnTarget != Int.MIN_VALUE ->
                        GraphEdge(regretEdge.sourceNodeIndex, matchingOnTarget, Unit)

                    else ->
                        GraphEdge(matchingOnSource, matchingOnTarget, Unit)
                }
                sequencesToTarget.apply {
                    set(regretEdge.sourceNodeIndex, Int.MIN_VALUE)
                    set(newSequence.targetNodeIndex, newSequence.sourceNodeIndex)
                }
                sequencesFromSource.apply {
                    set(regretEdge.targetNodeIndex, Int.MIN_VALUE)
                    set(newSequence.sourceNodeIndex, newSequence.targetNodeIndex)
                }

                sequentialRepresentation[regretEdge.sourceNodeIndex] = regretEdge.targetNodeIndex
                inverseSequentialRepresentation[regretEdge.targetNodeIndex] = regretEdge.sourceNodeIndex
                selectedEdges++
            }

        graphEdges = graphEdges
            .map {
                it.asSequence()
                    .filter { edge -> sequentialRepresentation[edge.sourceNodeIndex] == Int.MIN_VALUE }
                    .filter { edge -> inverseSequentialRepresentation[edge.targetNodeIndex] == Int.MIN_VALUE }
                    .filter { edge -> sequentialRepresentation[edge.targetNodeIndex] != edge.sourceNodeIndex }
                    .filter { edge -> sequencesFromSource[edge.targetNodeIndex] != edge.sourceNodeIndex }
                    .filter { edge -> sequencesToTarget[edge.sourceNodeIndex] != edge.targetNodeIndex }
                    .toList()
            }
            .filter { it.isNotEmpty() }
    }

    val missingEdge = sequencesFromSource.withIndex()
        .first { it.value != Int.MIN_VALUE }
        .let { GraphEdge(sourceNodeIndex = it.value, targetNodeIndex = it.index, value = Unit) }

    sequentialRepresentation[missingEdge.sourceNodeIndex] = missingEdge.targetNodeIndex
    return toPermutation(sequentialRepresentation)

}


private fun toPermutation(bestSequentialRepresentation: IntArray): IntArray {
    val result = IntArray(bestSequentialRepresentation.size - 1) { -1 }
    result[0] = bestSequentialRepresentation.last()
    (1..<result.size).forEach {
        result[it] = bestSequentialRepresentation[result[it - 1]]
    }
    return result
}
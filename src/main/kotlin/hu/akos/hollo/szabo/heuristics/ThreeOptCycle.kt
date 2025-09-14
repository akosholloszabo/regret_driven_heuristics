package hu.akos.hollo.szabo.heuristics

import hu.akos.hollo.szabo.model.GraphEdge
import hu.akos.hollo.szabo.model.RegretData
import hu.akos.hollo.szabo.model.dimensions
import hu.akos.hollo.szabo.model.get
import kotlin.math.abs


fun threeOptCycle(
    permutation: IntArray,
    regretRecord: RegretData,
    regretMatrix: Array<DoubleArray>,
    cost: Double
): Double {
    var bestCost = cost
    val positions = IntArray(permutation.size + 1) { it }

    val edgesOrderedWithIndex = positions
        .sortedByDescending { position -> positionToWeight(position, permutation, regretMatrix) }
        .withIndex()
        .toList()
        .slice(if (regretMatrix.dimensions.first == 500) 0..205 else positions.indices)

    outer@ for (firstPosition in edgesOrderedWithIndex.slice(0 until edgesOrderedWithIndex.size - 2)) {
        for (secondPosition in edgesOrderedWithIndex.slice(firstPosition.index + 1 until edgesOrderedWithIndex.size - 1)) {
            for (thirdPosition in edgesOrderedWithIndex.slice(secondPosition.index + 1 until edgesOrderedWithIndex.size)) {

                val selectedPositions = intArrayOf(firstPosition.value, secondPosition.value, thirdPosition.value)
                    .sorted()
                    .toIntArray()

                val edgesRemoved = selectedPositions
                    .map {
                        GraphEdge(
                            positionToVertexIndex(it, permutation),
                            positionToVertexIndex((it + 1) % positions.size, permutation),
                            positionToWeight(it, permutation, regretMatrix),
                        )
                    }

                val edgesAdded = arrayOf(
                    GraphEdge(
                        edgesRemoved[0].sourceNodeIndex,
                        edgesRemoved[1].targetNodeIndex,
                        regretMatrix[
                            edgesRemoved[0].sourceNodeIndex,
                            edgesRemoved[1].targetNodeIndex,
                        ],
                    ),
                    GraphEdge(
                        edgesRemoved[2].sourceNodeIndex,
                        edgesRemoved[0].targetNodeIndex,
                        regretMatrix[
                            edgesRemoved[2].sourceNodeIndex,
                            edgesRemoved[0].targetNodeIndex,
                        ],
                    ),
                    GraphEdge(
                        edgesRemoved[1].sourceNodeIndex,
                        edgesRemoved[2].targetNodeIndex,
                        regretMatrix[
                            edgesRemoved[1].sourceNodeIndex,
                            edgesRemoved[2].targetNodeIndex,
                        ],
                    )
                )

                val costRemoved = edgesRemoved
                    .sumOf { edge ->
                        regretRecord.distanceMatrix[
                            edge.sourceNodeIndex,
                            edge.targetNodeIndex,
                        ]
                    }
                val costAdded = edgesAdded.asSequence()
                    .sumOf { edge ->
                        regretRecord.distanceMatrix[
                            edge.sourceNodeIndex,
                            edge.targetNodeIndex,
                        ]
                    }

                if (costRemoved < costAdded) {
                    continue
                }

                val costDiff = costDiffOfThreeOptMove(permutation, selectedPositions, regretRecord.distanceMatrix)

                if (costDiff <= 0) {
                    applyThreeOptMove(permutation, selectedPositions)
                    bestCost += costDiff
                    if (abs(bestCost - calcCostOfTspSolution(permutation, regretRecord.distanceMatrix)) > 0.0001) {
                        throw Exception(
                            "Values should match! $costDiff $bestCost != ${
                                calcCostOfTspSolution(
                                    permutation,
                                    regretRecord.distanceMatrix
                                )
                            }"
                        )
                    }
                }
            }
        }
    }

    return bestCost
}

private fun positionToWeight(
    position: Int,
    permutation: IntArray,
    matrix: Array<DoubleArray>,
): Double =
    when (position) {
        0 -> matrix[permutation.size, permutation[0]]
        permutation.size -> matrix[permutation[permutation.size - 1], permutation.size]
        else -> matrix[permutation[position - 1], permutation[position]]
    }

private fun positionToVertexIndex(
    position: Int,
    permutation: IntArray,
): Int =
    when (position) {
        0 -> permutation.size
        else -> permutation[position - 1]
    }
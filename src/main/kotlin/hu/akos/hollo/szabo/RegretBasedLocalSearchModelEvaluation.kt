package hu.akos.hollo.szabo

//PROTECTED

import hu.akos.hollo.szabo.utils.DoubleMatrix
import hu.akos.hollo.szabo.utils.GraphEdge
import hu.akos.hollo.szabo.utils.Permutation
import hu.akos.hollo.szabo.utils.asImmutable
import java.io.File
import kotlin.math.sqrt
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

//size 100, 5s, 8x 3-opt
//size 150, 5s,
// 5s,7s,7s 30 samples
fun main() {
    (arrayOf(
        Pair(9, 100),
        Pair(8, 100),
        Pair(0, 100),
        Pair(2, 150),
        Pair(1, 150),
        Pair(0, 150),
        Pair(1, 250),
        Pair(0, 250),
        Pair(1, 500),
        Pair(0, 500),
    )).forEach { (threeOptIteration, modelSize) ->

        val regretData = loadRegrets(
            File(
                "H:\\by-domain\\work_and_learning_broken\\PhD\\research\\datasets\\2024_10_17_final_result_v1_atsp",
                "test_atsp$modelSize"
            )
        )

        val costDataPerNoiseLevelAndInstance =
            regretData
                .mapIndexed { regretRecordIndex, regretRecord ->
                    measureTimedValue {
                        val permutation = oneShotEdgeBuilder(regretRecord.predictedRegretMatrix)

                        val initialCost = calcCostOfTspSolution(permutation, regretRecord.distanceMatrix)

                        var bestCost = initialCost
                        measureTime {
                            repeat(threeOptIteration) {
                                bestCost = threeOptCycle(
                                    permutation,
                                    regretRecord,
                                    regretRecord.predictedRegretMatrix,
                                    bestCost
                                )
                            }
                        }//.let { print(it) }

                        CostRecord(
                            initialCost = initialCost,
                            builtCost = initialCost,
                            optimizedCost = bestCost,
                            optimal = regretRecord.optCost
                        )
                    }.also {
                        println("Regret record $regretRecordIndex: processing instance took: ${it.duration} gap: ${it.value.optimizedCost / it.value.optimal - 1.0}")
                    }
                }
                .toList()
        println(
            costDataPerNoiseLevelAndInstance.map { it.value.optimizedCost / it.value.optimal - 1.0 }.toDoubleArray()
                .statistics()
        )
        println(
            costDataPerNoiseLevelAndInstance.map { it.duration.inWholeMilliseconds.toDouble() }.toDoubleArray()
                .statistics()
        )
    }
}

data class CostRecord(
    val initialCost: Double,
    val builtCost: Double,
    val optimizedCost: Double,
    val optimal: Double,
)

data class DoubleStatistics(
    val max: Double,
    val min: Double,
    val avg: Double,
    val q1: Double,
    val q2: Double,
    val median: Double,
    val standardDeviation: Double,
)

fun DoubleArray.statistics(): DoubleStatistics {
    val sorted = sorted().toDoubleArray()
    val average = average()
    val standardDeviation = map { it - average }
        .map { it * it }
        .average()
        .let { sqrt(it) }
    return DoubleStatistics(
        max = sorted.last(),
        min = sorted.first(),
        avg = average,
        median = sorted[size / 2],
        q1 = sorted[size / 4],
        q2 = sorted[size * 3 / 4],
        standardDeviation = standardDeviation
    )
}

fun threeOptCycle(
    permutation: Permutation,
    regretRecord: RegretData,
    regretMatrix: DoubleMatrix,
    cost: Double
): Double {
    var bestCost = cost
    val positions = IntArray(permutation.size + 1) { it }

    val edgesOrderedWithIndex = positions
        .sortedByDescending { position -> positionToWeight(position, permutation, regretMatrix) }
        .withIndex()
        .toList()
        .slice(if (regretMatrix.dimensions.x == 500) 0..205 else positions.indices)
    val threeOptOperator = ThreeOptOperator()

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

                threeOptOperator.apply(permutation, selectedPositions.asImmutable())
                val newCost = calcCostOfTspSolution(permutation, regretRecord.distanceMatrix)
                if (newCost <= bestCost) {
                    bestCost = newCost
                } else {
                    //TODO: NOT EFFICIENT!
                    threeOptOperator.revert(permutation, selectedPositions.asImmutable())
                }
            }
        }
    }

    return bestCost
}

private fun positionToWeight(
    position: Int,
    permutation: Permutation,
    matrix: DoubleMatrix,
): Double =
    when (position) {
        0 -> matrix[permutation.size, permutation[0]]
        permutation.size -> matrix[permutation[permutation.size - 1], permutation.size]
        else -> matrix[permutation[position - 1], permutation[position]]
    }

private fun positionToVertexIndex(
    position: Int,
    permutation: Permutation,
): Int =
    when (position) {
        0 -> permutation.size
        else -> permutation[position - 1]
    }

fun calcCostOfTspSolution(permutation: Permutation, distanceMatrix: DoubleMatrix): Double =
    (1 until permutation.size)
        .sumOf { index ->
            distanceMatrix[permutation[index - 1], permutation[index]]
        } +
            distanceMatrix[permutation.last(), permutation.size] +
            distanceMatrix[permutation.size, permutation[0]]
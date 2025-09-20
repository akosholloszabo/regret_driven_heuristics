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

private val DEFAULT_TRIAL_DIRECTORIES = listOf(
    "/project/c_gnn_001/code/tsp/atsp_gnn/jobs/search2/12201357/best_model_rel_pp_ss_st_tt_attn_export/best_model_rel_pp_ss_st_tt_attn/trial_0",
).map(::File)
private const val TRIAL_DIRECTORIES_ENV = "TRIAL_DIRECTORIES"

private val iterationScheduleBySize = mapOf(
    1000 to listOf(0, 1),
)

data class ModelMetadata(
    val aggregation: String,
    val relations: List<String>,
) {
    val relationLabel: String = relations.joinToString("+")
}

fun main() {
    val explicitTrialDirectories = System.getenv(TRIAL_DIRECTORIES_ENV)
        ?.takeIf { it.isNotBlank() }
        ?.split(File.pathSeparator)
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.map(::File)
        ?.takeIf { it.isNotEmpty() }

    val trialDirectories = (explicitTrialDirectories ?: DEFAULT_TRIAL_DIRECTORIES)
        .distinctBy { it.absolutePath }
        .sortedBy { it.absolutePath }

    if (trialDirectories.isEmpty()) {
        println("No trial directories configured.")
        return
    }

    for (trialDir in trialDirectories) {
        if (!trialDir.isDirectory) {
            println("Skipping ${trialDir.absolutePath}: not a directory")
            continue
        }

        val modelDir = trialDir.parentFile
        if (modelDir == null || !modelDir.isDirectory) {
            println("Skipping ${trialDir.absolutePath}: missing parent model directory")
            continue
        }

        val metadata = modelDir.toModelMetadata()
        if (metadata == null) {
            println("Skipping ${trialDir.absolutePath}: parent directory does not match expected pattern")
            continue
        }

        for ((modelSize, iterationCounts) in iterationScheduleBySize) {
            val dataDir = File(trialDir, "test_atsp$modelSize")
            if (!dataDir.isDirectory) {
                println("Skipping ${trialDir.absolutePath} ATSP$modelSize: missing ${dataDir.name}")
                continue
            }

            val regretData = loadRegrets(dataDir)
            if (regretData.isEmpty()) {
                println("Skipping ${trialDir.absolutePath} ATSP$modelSize: no instances found")
                continue
            }

            for (threeOptIteration in iterationCounts) {
                val costDataPerInstance = regretData
                    .map { regretRecord ->
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
                            }

                            CostRecord(
                                initialCost = initialCost,
                                builtCost = initialCost,
                                optimizedCost = bestCost,
                                optimal = regretRecord.optCost
                            )
                        }
                    }

                val gapStats = costDataPerInstance
                    .map { (it.value.optimizedCost / it.value.optimal - 1.0) * 100 }
                    .toDoubleArray()
                    .statistics()

                val timeStats = costDataPerInstance
                    .map { it.duration.inWholeMilliseconds.toDouble() / 1000.0 }
                    .toDoubleArray()
                    .statistics()

                val numInstances = costDataPerInstance.size

                println(
                    "${trialDir.absolutePath} | ATSP$modelSize | iterations: $threeOptIteration | Agg: ${metadata.aggregation} | Rel: ${metadata.relationLabel} | Instances: $numInstances | Avg Gap: %.3f %% | Total Time: %.3f s"
                        .format(gapStats.avg, timeStats.sum)
                )
            }
        }
    }
}

private fun File.toModelMetadata(): ModelMetadata? {
    val prefix = "best_model_rel_"
    if (!name.startsWith(prefix)) return null

    val tokens = name.removePrefix(prefix).split("_")
    if (tokens.size < 2) return null

    val aggregation = tokens.last()
    val relations = tokens.dropLast(1)
    return ModelMetadata(aggregation = aggregation, relations = relations)
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
    val sum: Double,
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
        standardDeviation = standardDeviation,
        sum = sum()
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

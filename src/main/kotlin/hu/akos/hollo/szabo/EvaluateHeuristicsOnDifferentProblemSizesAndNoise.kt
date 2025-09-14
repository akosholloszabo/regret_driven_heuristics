package hu.akos.hollo.szabo

import hu.akos.hollo.szabo.heuristics.calcCostOfTspSolution
import hu.akos.hollo.szabo.heuristics.oneShotEdgeBuilder
import hu.akos.hollo.szabo.heuristics.threeOptCycle
import hu.akos.hollo.szabo.model.CostRecord
import hu.akos.hollo.szabo.model.loadRegrets
import hu.akos.hollo.szabo.model.statistics
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

//TODO: Use KMath lib for platform independence

fun main() {
    sequenceOf(100, 150, 250, 500).forEach { problemSize ->
        (0..100).asSequence().map { it / 100.0 }.forEach { noiseLevel ->
            (0..10).asSequence().forEach { threeOptIteration ->
                evaluate(problemSize, threeOptIteration, noiseLevel)
            }
        }
    }
}

private fun evaluate(threeOptIteration: Int, modelSize: Int, noiseLevel: Double) {

    val sampleCount = 10

    val regretData = loadRegrets(
        File(
            "H:\\by-domain\\work_and_learning_broken\\PhD\\research\\datasets\\2024_10_17_final_result_v1_atsp",
            "test_atsp$modelSize"
        )
    ).flatMap { regretRecord ->
        (0 until sampleCount).asSequence().map {
            regretRecord.copy(
                predictedRegretMatrix = Array(regretRecord.expectedRegretMatrix.size) { index ->
                    regretRecord.expectedRegretMatrix[index]
                        .map { regretValue->
                            Random().nextGaussian(1.0, noiseLevel)
                                .let { max(0.0, it) }
                                .times(regretValue)
                        }
                        .toDoubleArray()
                }
            )
        }
    }

    val costDataPerInstance = regretData
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
                }

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
        costDataPerInstance.map { it.value.optimizedCost / it.value.optimal - 1.0 }.toDoubleArray()
            .statistics()
    )
    println(
        costDataPerInstance.map { it.duration.inWholeMilliseconds.toDouble() }.toDoubleArray()
            .statistics()
    )
}
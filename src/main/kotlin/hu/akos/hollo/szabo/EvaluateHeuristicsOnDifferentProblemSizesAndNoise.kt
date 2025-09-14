package hu.akos.hollo.szabo

import hu.akos.hollo.szabo.heuristics.calcCostOfTspSolution
import hu.akos.hollo.szabo.heuristics.oneShotEdgeBuilder
import hu.akos.hollo.szabo.heuristics.threeOptCycle
import hu.akos.hollo.szabo.model.CostRecord
import hu.akos.hollo.szabo.model.loadRegrets
import hu.akos.hollo.szabo.model.statistics
import hu.akos.hollo.szabo.model.get
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

//TODO: Use KMath lib for platform independence

fun main() {
    File("output/output.txt").writeText("")
    sequenceOf(64/*100 , 150, 250, 500*/).forEach { problemSize ->
        (0..100).asSequence().map { it / 100.0 }.forEach { noiseLevel ->
            (0..10).asSequence().forEach { threeOptIteration ->
                evaluate( threeOptIteration, problemSize,noiseLevel)
            }
        }
    }
}

private fun evaluate(threeOptIteration: Int, modelSize: Int, noiseLevel: Double) {
    File("output/output.txt").appendText("NEW RUN: modelSize: $modelSize, noiseLevel: $noiseLevel, threeOptIteration: $threeOptIteration\n")
    val sampleCount = 10

    val regretData = loadRegrets(
//        File(
//            "H:\\by-domain\\official\\phd\\research\\datasets\\2024_10_17_final_result_v1_atsp",
//            "test_atsp$modelSize"
//        )
                File(
            "H:\\by-domain\\official\\phd\\research\\datasets\\2024_10_05_test_model4"
        )
    ).flatMap { regretRecord ->
        (0 until sampleCount).asSequence().map {
            regretRecord.copy(
                predictedRegretMatrix = Array(regretRecord.expectedRegretMatrix.size) { index ->
                    DoubleArray(regretRecord.expectedRegretMatrix.size) { innerIndex ->
                        regretRecord.expectedRegretMatrix[innerIndex, index ].let {regretValue->
                            Random().nextGaussian(1.0, noiseLevel)
                                .let { max(0.0, it) }
                                .times(regretValue)
                        }
                    }
//                    regretRecord.expectedRegretMatrix[index]
//                        .map { regretValue->
//                            Random().nextGaussian(1.0, noiseLevel)
//                                .let { max(0.0, it) }
//                                .times(regretValue)
//                        }
//                        .toDoubleArray()
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
                File("output/output.txt").appendText("Regret record $regretRecordIndex: processing instance took: ${it.duration} gap: ${it.value.optimizedCost / it.value.optimal - 1.0} \n")
            }
        }
        .toList()
    File("output/output.txt").appendText(
        costDataPerInstance.map { it.value.optimizedCost / it.value.optimal - 1.0 }.toDoubleArray()
            .statistics().toString().plus("\n")
    )
    File("output/output.txt").appendText(
        costDataPerInstance.map { it.duration.inWholeMilliseconds.toDouble() }.toDoubleArray()
            .statistics().toString().plus("\n")
    )
}
package hu.akos.hollo.szabo

import hu.akos.hollo.szabo.heuristics.calcCostOfTspSolution
import hu.akos.hollo.szabo.heuristics.oneShotEdgeBuilder
import hu.akos.hollo.szabo.heuristics.threeOptCycle
import hu.akos.hollo.szabo.model.*
import java.io.File
import java.time.LocalDate
import java.util.*
import kotlin.math.absoluteValue
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

//TODO: Use KMath lib for platform independence

fun main() {
    val currentDate = LocalDate.now()
    println(currentDate.toString())
    File("output/output-${currentDate}-2.txt").writeText("")
    val regretData = loadRegrets(
        File("H:\\by-domain\\official\\phd\\research\\datasets\\2025_09_18_atsp50_for_noise_analyses")
    ).filter {
        it.expectedRegretMatrix.all { it.count { it == 0.0 } == 2 }
    }.filter {
        val permutation = oneShotEdgeBuilder(it.expectedRegretMatrix)
        val initialCost = calcCostOfTspSolution(permutation, it.distanceMatrix)
        initialCost == it.optCost
    }
    println("Data loaded: ${regretData.size} instances")
    (0..5).asSequence().forEach { threeOptIteration ->
        (0..50 step 2).asSequence().map { it / 100.0 }.forEach { noiseLevel ->
            evaluate(threeOptIteration, regretData, noiseLevel)
        }
    }
}

private fun evaluate(threeOptIteration: Int, _regretData: List<RegretData>, noiseLevel: Double) {
    val currentDate = LocalDate.now()
    File("output/output-${currentDate}-2.txt").appendText("NEW RUN: noiseLevel: $noiseLevel, threeOptIteration: $threeOptIteration\n")
    val sampleCount = 10
    val regretData = _regretData.flatMap { regretRecord ->
        val max = regretRecord.expectedRegretMatrix.maxOf { it.max() }
        (0 until sampleCount).asSequence().map {
            regretRecord.copy(
                predictedRegretMatrix = Array(regretRecord.expectedRegretMatrix.size) { index ->
                    DoubleArray(regretRecord.expectedRegretMatrix.size) { innerIndex ->
                        regretRecord.expectedRegretMatrix[index, innerIndex].let { regretValue ->
                            Random().nextGaussian(0.0, noiseLevel)
                                .absoluteValue
                                .times(max)
                                .plus(regretValue)
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
                //File("output/output-${currentDate}-2.txt").appendText("Regret record $regretRecordIndex: processing instance took: ${it.duration} gap: ${it.value.optimizedCost / it.value.optimal - 1.0} \n")
                val expectedEdges = regretRecord.expectedRegretMatrix
                    .mapIndexed { index, it ->
                        it.withIndex().filter { it.index != index }.filter { it.value == 0.0 }
                    }
                    .mapIndexed { index, it -> it.map { index to it.index } }
            }
        }
        .toList()
    File("output/output-${currentDate}-2.txt").appendText(
        costDataPerInstance.map { it.value.optimizedCost / it.value.optimal - 1.0 }.toDoubleArray()
            .statistics().toString().plus("\n")
    )
    File("output/output-${currentDate}-2.txt").appendText(
        costDataPerInstance.map { it.duration.inWholeMilliseconds.toDouble() }.toDoubleArray()
            .statistics().toString().plus("\n")
    )
}
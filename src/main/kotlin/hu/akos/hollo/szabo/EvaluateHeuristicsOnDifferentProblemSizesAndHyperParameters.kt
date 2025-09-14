package hu.akos.hollo.szabo

//PROTECTED

import hu.akos.hollo.szabo.heuristics.calcCostOfTspSolution
import hu.akos.hollo.szabo.heuristics.oneShotEdgeBuilder
import hu.akos.hollo.szabo.heuristics.threeOptCycle
import hu.akos.hollo.szabo.model.CostRecord
import hu.akos.hollo.szabo.model.loadRegrets
import hu.akos.hollo.szabo.model.statistics
import java.io.File
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
                "H:\\by-domain\\official\\phd\\research\\datasets\\2024_10_17_final_result_v1_atsp",
                "test_atsp$modelSize"
            )
        )

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
                    //println("Regret record $regretRecordIndex: processing instance took: ${it.duration} gap: ${it.value.optimizedCost / it.value.optimal - 1.0}")
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
}

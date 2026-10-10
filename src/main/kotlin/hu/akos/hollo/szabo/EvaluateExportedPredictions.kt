package hu.akos.hollo.szabo

import hu.akos.hollo.szabo.heuristics.calcCostOfTspSolution
import hu.akos.hollo.szabo.heuristics.oneShotEdgeBuilder
import hu.akos.hollo.szabo.heuristics.threeOptCycle
import hu.akos.hollo.szabo.model.loadRegrets
import java.io.File
import kotlin.time.measureTimedValue

// usage: <folder with instance*.txt> <maximum number of 3-opt rounds K>
// One JSON line per instance: the cost of the one-shot edge builder (round 0) and after each of the 3-opt rounds 1..K, with the cumulative milliseconds at each point
// (the rounds are applied one after the other to the same tour, which equals K separate runs because the heuristic is deterministic).
fun main(args: Array<String>) {
    val regretData = loadRegrets(File(args[0]))
    val names = File(args[0]).listFiles()!!.map { it.name }.filter { !it.endsWith("results.json") }   // loadRegrets reads the same listing in the same order and skips results.json
    val rounds = args[1].toInt()
    regretData.forEachIndexed { index, record ->
        val costs = ArrayList<Double>()
        val millis = ArrayList<Long>()
        var elapsed = 0L
        val built = measureTimedValue { oneShotEdgeBuilder(record.predictedRegretMatrix) }
        val permutation = built.value
        var best = calcCostOfTspSolution(permutation, record.distanceMatrix)
        elapsed += built.duration.inWholeMicroseconds
        costs.add(best); millis.add(elapsed)
        repeat(rounds) {
            val step = measureTimedValue { threeOptCycle(permutation, record, record.predictedRegretMatrix, best) }
            best = step.value
            elapsed += step.duration.inWholeMicroseconds
            costs.add(best); millis.add(elapsed)
        }
        println("""{"index": $index, "file": "${names[index]}", "opt": ${record.optCost}, "costs": [${costs.joinToString(",")}], "micros": [${millis.joinToString(",")}]}""")
    }
}

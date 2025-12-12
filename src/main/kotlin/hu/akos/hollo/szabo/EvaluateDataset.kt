package hu.akos.hollo.szabo

import hu.akos.hollo.szabo.model.loadRegrets
import hu.akos.hollo.szabo.model.statistics
import java.io.File
import java.time.LocalDate

//DoubleStatistics(max=3.22, min=1.0, avg=1.4506399999999964, q1=1.24, q2=1.6, median=1.4, standardDeviation=0.29620501106804265)
//DoubleStatistics(max=2.102747136E9, min=-2.145058816E9, avg=9386907.811636364, q1=512.0, q2=1769472.0, median=32768.0, standardDeviation=3.774513013624974E8)
fun main() {
    val currentDate = LocalDate.now()
    println(currentDate.toString())
    File("output/output-${currentDate}.txt").writeText("")
    val regretData = loadRegrets(
        File("H:\\by-domain\\official\\phd\\research\\datasets\\2025_09_18_atsp50_for_noise_analyses")
    )
    regretData.asSequence()
        .map { it.expectedRegretMatrix.sumOf { it.count { it == 0.0 } } }
        .map { it - 50 }
        .map { it / 50.0 }
        .toList()
        .toDoubleArray()
        .statistics()
        .also { println(it) }
    regretData.asSequence()
        .map {
            it.expectedRegretMatrix
                .map { it.count { it == 0.0 } - 1 }
                .reduce { a, b -> a * b } }
        .map { it.toDouble() }
        .toList()
        .toDoubleArray()
        .statistics()
        .also { println(it) }
}
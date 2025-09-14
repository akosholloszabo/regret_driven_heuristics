package hu.akos.hollo.szabo.model

import kotlin.math.sqrt

operator fun Array<DoubleArray>.get(firstIndex: Int, secondIndex: Int): Double = this[firstIndex][secondIndex]

fun <T> Array<DoubleArray>.mapEachEntryIndexed(mapper: (columnIndex: Int, rowIndex: Int, value: Double) -> T) =
    asSequence().mapIndexed { columnIndex, column ->
        column.asSequence().mapIndexed { rowIndex, value ->
            mapper(columnIndex, rowIndex, value)
        }
    }

val Array<DoubleArray>.dimensions get() = Pair(size, this[0].size)


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
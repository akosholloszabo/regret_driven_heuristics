package hu.akos.hollo.szabo.utils

operator fun Array<DoubleArray>.get(firstIndex: Int, secondIndex: Int): Double = this[firstIndex][secondIndex]

val IntArray.x: Int get() = get(0)
val IntArray.y: Int get() = get(1)

fun <T> Array<DoubleArray>.mapEachEntryIndexed(mapper: (columnIndex: Int, rowIndex: Int, value: Double) -> T) =
    asSequence().mapIndexed { columnIndex, column ->
        column.asSequence().mapIndexed { rowIndex, value ->
            mapper(columnIndex, rowIndex, value)
        }
    }

val Array<DoubleArray>.dimensions get() = intArrayOf(size, this[0].size)
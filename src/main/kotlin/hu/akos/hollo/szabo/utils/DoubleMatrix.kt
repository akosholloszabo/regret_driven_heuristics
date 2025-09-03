package hu.akos.hollo.szabo.utils

import hu.akos.hollo.szabo.utils.DoubleVector.Companion.dot
import hu.akos.hollo.szabo.utils.DoubleVector.Companion.minus
import hu.akos.hollo.szabo.utils.DoubleVector.Companion.plus
import hu.akos.hollo.szabo.utils.DoubleVector.Companion.times
import hu.akos.hollo.szabo.utils.ImmutableArray.Companion.immutableArrayOf

@JvmInline
value class DoubleMatrix(val columnVectors: ImmutableArray<DoubleVector>) {

    companion object {
        fun Array<DoubleVector>.asMatrixColumns() = DoubleMatrix(this.asImmutable())
    }

    init {
        require(columnVectors.asSequence().all { it.size == columnVectors[0].size })
    }

    constructor(size: IntVector2D, initializer: (indexes: IntVector2D) -> Double)
            : this(
        Array(size.x) { columnIndex ->
            DoubleVector(size.y) { lineIndex: Int ->
                initializer(
                    IntVector2D(columnIndex, lineIndex)
                )
            }
        }.asImmutable()
    )

    fun <T> mapEachEntryIndexed(mapper: (columnIndex: Int, rowIndex: Int, value: Double) -> T) =
        columnVectors.asSequence().mapIndexed { columnIndex, column ->
            column.asSequence().mapIndexed { rowIndex, value ->
                mapper(columnIndex, rowIndex, value)
            }
        }

    val dimensions get() = IntVector2D(columnVectors.size, columnVectors[0].size)
    val indices get() = immutableArrayOf(columnVectors.indices, columnVectors[0].indices)

    operator fun get(indexes: IntVector2D) = columnVectors[indexes.x][indexes.y]
    operator fun get(columnIndex: Int, rowIndex: Int) = columnVectors[columnIndex][rowIndex]

    operator fun get(index: Int) = columnVectors[index]
    fun getRow(rowIndex: Int) =
        DoubleVector(columnVectors.size) { columnIndex -> columnVectors[columnIndex][rowIndex] }


    operator fun minus(other: DoubleMatrix): DoubleMatrix {
        if (
            columnVectors.size != other.columnVectors.size ||
            columnVectors[0].size != other.columnVectors[0].size
        ) throw IndexOutOfBoundsException("Matrices are not the right size!")

        return Array(columnVectors.size) { columnIndex: Int ->
            columnVectors[columnIndex] - other.columnVectors[columnIndex]
        }.asMatrixColumns()
    }

    operator fun plus(other: DoubleMatrix): DoubleMatrix {
        if (
            columnVectors.size != other.columnVectors.size ||
            columnVectors[0].size != other.columnVectors[0].size
        ) throw IndexOutOfBoundsException("Matrices are not the right size!")

        return Array(columnVectors.size) { columnIndex: Int ->
            columnVectors[columnIndex] + other.columnVectors[columnIndex]
        }.asMatrixColumns()
    }

    operator fun times(other: Double) =
        Array(columnVectors.size) { columnIndex: Int ->
            columnVectors[columnIndex] * other
        }.asMatrixColumns()

    operator fun times(other: DoubleMatrix): DoubleMatrix {
        if (columnVectors.size != other.columnVectors[0].size)
            throw IndexOutOfBoundsException("Matrices are not the right size!")

        return DoubleMatrix(
            IntVector2D(
                other.columnVectors.size,
                columnVectors[0].size
            )
        ) { coordinates: IntVector2D ->
            getRow(coordinates.y).dot(other.columnVectors[coordinates.x])
        }
    }
}
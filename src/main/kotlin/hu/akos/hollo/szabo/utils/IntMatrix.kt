package hu.akos.hollo.szabo.utils

import hu.akos.hollo.szabo.utils.ImmutableArray.Companion.immutableArrayOf
import hu.akos.hollo.szabo.utils.IntVector.Companion.dot
import hu.akos.hollo.szabo.utils.IntVector.Companion.minus
import hu.akos.hollo.szabo.utils.IntVector.Companion.plus
import hu.akos.hollo.szabo.utils.IntVector.Companion.times


@JvmInline
value class IntMatrix(val columnVectors: ImmutableArray<IntVector>) {

    companion object {
        fun Array<IntVector>.asMatrixColumns() = IntMatrix(this.asImmutable())
    }

    init {
        require(columnVectors.asSequence().all { it.size == columnVectors[0].size })
    }

    constructor(size: IntVector2D, initializer: (indexes: IntVector2D) -> Int)
            : this(
        Array(size.x) { columnIndex ->
            IntVector(size.y) { lineIndex: Int ->
                initializer(
                    IntVector2D(columnIndex, lineIndex)
                )
            }
        }.asImmutable()
    )

    operator fun get(indexes: IntVector2D) = columnVectors[indexes.x][indexes.y]
    operator fun get(columnIndex: Int, rowIndex: Int) = columnVectors[columnIndex][rowIndex]
    operator fun get(index: Int) = columnVectors[index]
    fun getRow(index: Int) = IntVector(columnVectors.size) { columnIndex -> columnVectors[columnIndex][index] }


    operator fun minus(other: IntMatrix): IntMatrix {
        if (
            columnVectors.size != other.columnVectors.size ||
            columnVectors[0].size != other.columnVectors[0].size
        )
            throw IndexOutOfBoundsException("Matrices are not the right size!")

        return Array(columnVectors.size) { columnIndex: Int ->
            columnVectors[columnIndex] - other.columnVectors[columnIndex]
        }.asMatrixColumns()
    }

    operator fun plus(other: IntMatrix): IntMatrix {
        if (
            columnVectors.size != other.columnVectors.size ||
            columnVectors[0].size != other.columnVectors[0].size
        )
            throw IndexOutOfBoundsException("Matrices are not the right size!")

        return Array(columnVectors.size) { columnIndex: Int ->
            columnVectors[columnIndex] + other.columnVectors[columnIndex]
        }.asMatrixColumns()
    }

    operator fun times(other: Int) =
        Array(columnVectors.size) { lineIndex: Int ->
            columnVectors[lineIndex] * other
        }.asMatrixColumns()


    operator fun times(other: IntMatrix): IntMatrix {
        if (columnVectors.size != other.columnVectors[0].size)
            throw IndexOutOfBoundsException("Matrices are not the right size!")

        return IntMatrix(
            IntVector2D(
                other.columnVectors.size,
                columnVectors[0].size
            )
        ) { coordinates: IntVector2D ->
            getRow(coordinates.y).dot(other.columnVectors[coordinates.x])
        }
    }
}
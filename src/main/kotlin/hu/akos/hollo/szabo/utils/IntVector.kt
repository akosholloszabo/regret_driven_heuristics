package hu.akos.hollo.szabo.utils

import kotlin.math.sqrt

//Unfortunately, Kotlin does not support inline classes for arrays well enough yet.
//TODO: Replace it by ArrayList based variant or remove interface implementations
@JvmInline
value class IntVector(private val coordinates: IntArray) : List<Int> {

    constructor(size: Int, initializer: (it: Int) -> Int) : this(IntArray(size, initializer))

    companion object : VectorExtension<IntMatrix, IntVector, Int> {
        fun intVectorOf(vararg elements: Int) = IntVector(elements)

        override fun unitVector(size: Int, index: Int) =
            IntVector(size) {
                if (it == index) 1
                else 0
            }

//        override fun IntVector.div(other: IntVector): IntVector {
//            if (other.size != size)
//                throw IndexOutOfBoundsException("other array is not the same size!")
//
//            return IntVector(size) { index -> this[index] / other[index] }
//        }

        override fun nullVector(size: Int) = IntVector(size) { 0 }

        override fun fill(size: Int, value: Int) = IntVector(size) { value }

        override fun IntVector.length() = secondNorma()

        override fun IntVector.toDoubleVector() = DoubleVector(size) { this[it].toDouble() }

        override fun IntVector.fill(value: Int) = coordinates.fill(value)

        override operator fun IntVector.div(other: Int) = coordinates.run {
            IntVector(size) { index -> get(index) / other }
        }

        override infix fun IntVector.dot(other: IntVector) = coordinates.run {
            if (other.size != size)
                throw IndexOutOfBoundsException("other array is not the same size!")

            indices.sumOf { index -> this[index] * other[index] }
        }

        override infix fun IntVector.match(other: IntVector) = coordinates.run {
            other.size != size &&
                    indices.all { index -> this[index] == other[index] }
        }

        override operator fun IntVector.minus(other: IntVector) = coordinates.run {
            if (other.size != size)
                throw IndexOutOfBoundsException("other array is not the same size!")

            IntVector(size) { index -> get(index) - other[index] }
        }

        override operator fun IntVector.plus(other: IntVector) = coordinates.run {
            if (other.size != size)
                throw IndexOutOfBoundsException("other array is not the same size!")

            IntVector(size) { index -> get(index) + other[index] }
        }

        override operator fun IntVector.times(other: Int) = coordinates.run {
            IntVector(size) { index -> get(index) * other }
        }

//        override operator fun IntVector.times(other: IntVector) = coordinates.run {
//            if (other.size != size)
//                throw IndexOutOfBoundsException("other array is not the same size!")
//
//            IntVector(size) { index -> get(index) * other[index] }
//        }
//
//        override operator fun IntVector.times(other: IntMatrix): IntVector {
//            if (other.matrix.any { it.size != size })
//                throw IndexOutOfBoundsException("Matrices are not the right size!")
//
//            return IntVector(other.size) { index ->
//                other[index] dot this
//            }
//        }

        override fun IntVector.zeroNorma() = coordinates.count { it != 0 }

        override fun IntVector.firstNorma() = coordinates.sum()

        override fun IntVector.secondNorma() = coordinates.map { it * it }.sum().let { sqrt(it.toDouble()) }

        override fun IntVector.infiniteNorm() = coordinates.max()

        override fun IntVector.dominatesSmaller(other: IntVector) =
            indices.all { get(it) >= other[it] } &&
                    indices.any { get(it) > other[it] }

        override fun IntVector.dominatesBigger(other: IntVector) =
            indices.all { get(it) <= other[it] } &&
                    indices.any { get(it) < other[it] }

        override fun IntVector.volume() = coordinates.reduce { acc, i -> acc * i }

        override fun IntVector.set(index: Int, value: Int) {
            coordinates[index] = value
        }

    }

    //List implementation
    override val size: Int get() = coordinates.size
    override fun get(index: Int): Int = coordinates[index]
    override fun isEmpty(): Boolean = size == 0
    override fun iterator(): Iterator<Int> = coordinates.iterator()
    override fun listIterator(): ListIterator<Int> = coordinates.asList().listIterator()
    override fun listIterator(index: Int): ListIterator<Int> = coordinates.asList().listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<Int> = coordinates.slice(fromIndex..toIndex)
    override fun lastIndexOf(element: Int): Int = coordinates.withIndex().last { it.value == element }.index
    override fun indexOf(element: Int): Int = coordinates.indexOfFirst { it == element }
    override fun containsAll(elements: Collection<Int>): Boolean =
        elements.all { element -> coordinates.indexOfFirst { it == element } != -1 }

    override fun contains(element: Int): Boolean = coordinates.any { it == element }

    fun asIntArray() = coordinates
    fun toIntArray() = coordinates.clone()
}
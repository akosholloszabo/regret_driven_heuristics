package hu.akos.hollo.szabo.utils

import kotlin.math.sqrt

//Unfortunately, Kotlin does not support inline classes for arrays well enough yet.
//TODO: Replace it by ArrayList based variant or remove interface implementations
@JvmInline
value class DoubleVector(val coordinates: DoubleArray) : List<Double> {

    constructor(size: Int, initializer: (it: Int) -> Double) : this(DoubleArray(size, initializer))

    companion object : VectorExtension<DoubleMatrix, DoubleVector, Double> {
        override fun unitVector(size: Int, index: Int) =
            DoubleVector(size) {
                if (it == index) 1.0
                else 0.0
            }

        override fun DoubleVector.volume(): Double {
            return coordinates.reduce { acc, d -> acc * d }
        }

        override fun DoubleVector.set(index: Int, value: Double) {
            coordinates[index] = value
        }

        override fun nullVector(size: Int) = DoubleVector(size) { 0.0 }

        override fun fill(size: Int, value: Double) = DoubleVector(size) { value }

        override fun DoubleVector.length() = sqrt(this dot this)

        override fun DoubleVector.toDoubleVector() = DoubleVector(size) { this[it] }

        override fun DoubleVector.fill(value: Double) = coordinates.fill(value)

        override operator fun DoubleVector.div(other: Double) = coordinates.run {
            DoubleVector(size) { index -> get(index) / other }
        }

        override infix fun DoubleVector.dot(other: DoubleVector) = coordinates.run {
            if (other.size != size)
                throw IndexOutOfBoundsException("other array is not the same size!")

            indices.sumOf { index -> this[index] * other[index] }
        }

        override infix fun DoubleVector.match(other: DoubleVector) = coordinates.run {
            other.size != size &&
                    indices.all { index -> this[index] == other[index] }
        }

        override operator fun DoubleVector.minus(other: DoubleVector) = coordinates.run {
            if (other.size != size)
                throw IndexOutOfBoundsException("other array is not the same size!")

            DoubleVector(size) { index -> get(index) - other[index] }
        }

        override operator fun DoubleVector.plus(other: DoubleVector) = coordinates.run {
            if (other.size != size)
                throw IndexOutOfBoundsException("other array is not the same size!")

            DoubleVector(size) { index -> get(index) + other[index] }
        }

        override operator fun DoubleVector.times(other: Double) = coordinates.run {
            DoubleVector(size) { index -> get(index) * other }
        }

//        override operator fun DoubleVector.times(other: DoubleVector) = coordinates.run {
//            if (other.size != size)
//                throw IndexOutOfBoundsException("other array is not the same size!")
//
//            DoubleVector(size) { index -> get(index) * other[index] }
//        }
//
//        override operator fun DoubleVector.times(other: DoubleMatrix): DoubleVector {
//            if (other.matrix.any { it.size != this.size })
//                throw IndexOutOfBoundsException("Matrices are not the right size!")
//
//            return DoubleVector(size) { index -> other[index] dot this }
//        }

        override fun DoubleVector.zeroNorma() = coordinates.count { it == 0.0 }

        override fun DoubleVector.firstNorma() = coordinates.sumOf { it }

        override fun DoubleVector.secondNorma() = sqrt(coordinates.sumOf { it * it })

        override fun DoubleVector.infiniteNorm() = coordinates.maxOrNull() ?: 0.0

        override fun DoubleVector.dominatesSmaller(other: DoubleVector) =
            indices.all { get(it) >= other[it] } &&
                    indices.any { get(it) > other[it] }

        override fun DoubleVector.dominatesBigger(other: DoubleVector) =
            indices.all { get(it) <= other[it] } &&
                    indices.any { get(it) < other[it] }

//        override operator fun DoubleVector.div(other: DoubleVector) = coordinates.run {
//            if (other.size != size)
//                throw IndexOutOfBoundsException("other array is not the same size!")
//
//            DoubleVector(size) { index -> get(index) / other[index] }
//        }
    }

    fun asDoubleArray() = coordinates
    fun toDoubleArray() = coordinates.clone()

    //List implementation
    override val size: Int get() = coordinates.size
    override fun get(index: Int): Double = coordinates[index]
    override fun isEmpty(): Boolean = size == 0
    override fun iterator(): Iterator<Double> = coordinates.iterator()
    override fun listIterator(): ListIterator<Double> = coordinates.asList().listIterator()
    override fun listIterator(index: Int): ListIterator<Double> = coordinates.asList().listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<Double> = coordinates.slice(fromIndex..toIndex)
    override fun lastIndexOf(element: Double): Int = coordinates.withIndex().last { it.value == element }.index
    override fun indexOf(element: Double): Int = coordinates.indexOfFirst { it == element }
    override fun containsAll(elements: Collection<Double>): Boolean =
        elements.all { element -> coordinates.indexOfFirst { it == element } != -1 }

    override fun contains(element: Double): Boolean = coordinates.any { it == element }
}

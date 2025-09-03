package hu.akos.hollo.szabo.utils

interface VectorExtension<M, V, T> {
    fun fill(size: Int, value: T): V
    fun nullVector(size: Int): V
    fun unitVector(size: Int, index: Int): V
    operator fun V.set(index: Int, value: T)
    fun V.length(): Double
    fun V.zeroNorma(): Int
    fun V.firstNorma(): T
    fun V.secondNorma(): Double
    fun V.infiniteNorm(): T
    fun V.volume(): T
    infix fun V.dominatesSmaller(other: V): Boolean
    infix fun V.dominatesBigger(other: V): Boolean
    fun V.toDoubleVector(): DoubleVector

    fun V.fill(value: T)

    operator fun V.div(other: T): V

    //operator fun V.div(other: V): V
    infix fun V.dot(other: V): T
    infix fun V.match(other: V): Boolean
    operator fun V.minus(other: V): V
    operator fun V.plus(other: V): V
    operator fun V.times(other: T): V
    //operator fun V.times(other: V): V
    //operator fun V.times(other: M): V
}
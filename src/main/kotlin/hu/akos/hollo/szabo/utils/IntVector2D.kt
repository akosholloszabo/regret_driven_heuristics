package hu.akos.hollo.szabo.utils

//Unfortunately, Kotlin does not support inline classes for arrays well enough yet.
//TODO: Replace it by ArrayList based variant or remove interface implementations
@JvmInline
value class IntVector2D private constructor(private val coordinates: IntVector) : List<Int> by coordinates {
    constructor(x: Int, y: Int) : this(IntVector.intVectorOf(x, y))

    val x: Int get() = coordinates[0]
    val y: Int get() = coordinates[1]
}
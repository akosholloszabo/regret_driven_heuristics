package hu.akos.hollo.szabo.utils

fun <T> Array<T>.asImmutable() = ImmutableArray(this)
fun IntArray.asImmutable() = ImmutableIntArray(this)

operator fun <T> Array<Array<T>>.get(indexes: IntVector2D): T = this[indexes[0]][indexes[1]]
operator fun Array<BooleanArray>.get(indexes: IntVector2D): Boolean = this[indexes[0]][indexes[1]]
operator fun Array<ByteArray>.get(indexes: IntVector2D): Byte = this[indexes[0]][indexes[1]]
operator fun Array<CharArray>.get(indexes: IntVector2D): Char = this[indexes[0]][indexes[1]]
operator fun Array<DoubleArray>.get(indexes: IntVector2D): Double = this[indexes[0]][indexes[1]]
operator fun Array<FloatArray>.get(indexes: IntVector2D): Float = this[indexes[0]][indexes[1]]
operator fun Array<IntArray>.get(indexes: IntVector2D): Int = this[indexes[0]][indexes[1]]
operator fun Array<LongArray>.get(indexes: IntVector2D): Long = this[indexes[0]][indexes[1]]
operator fun Array<ShortArray>.get(indexes: IntVector2D): Short = this[indexes[0]][indexes[1]]

operator fun <T> Array<Array<T>>.get(firstIndex: Int, secondIndex: Int): T = this[firstIndex][secondIndex]
operator fun Array<BooleanArray>.get(firstIndex: Int, secondIndex: Int): Boolean = this[firstIndex][secondIndex]
operator fun Array<ByteArray>.get(firstIndex: Int, secondIndex: Int): Byte = this[firstIndex][secondIndex]
operator fun Array<CharArray>.get(firstIndex: Int, secondIndex: Int): Char = this[firstIndex][secondIndex]
operator fun Array<DoubleArray>.get(firstIndex: Int, secondIndex: Int): Double = this[firstIndex][secondIndex]
operator fun Array<FloatArray>.get(firstIndex: Int, secondIndex: Int): Float = this[firstIndex][secondIndex]
operator fun Array<IntArray>.get(firstIndex: Int, secondIndex: Int): Int = this[firstIndex][secondIndex]
operator fun Array<LongArray>.get(firstIndex: Int, secondIndex: Int): Long = this[firstIndex][secondIndex]
operator fun Array<ShortArray>.get(firstIndex: Int, secondIndex: Int): Short = this[firstIndex][secondIndex]
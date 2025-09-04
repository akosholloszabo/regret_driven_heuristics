package hu.akos.hollo.szabo.utils

@JvmInline
value class ImmutableIntArray(private val elements: IntArray) {
     val size: Int
        get() = elements.size

     operator fun get(index: Int): Int = elements[index]

}
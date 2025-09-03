package hu.akos.hollo.szabo.utils


@JvmInline
value class ImmutableArray<T>(private val elements: Array<T>) {
    val size: Int
        get() = elements.size
    val indices: IntRange
        get() = elements.indices

    companion object {
        inline fun <reified U> immutableArrayOf(vararg args: U): ImmutableArray<U> = ImmutableArray(arrayOf(*args))
    }

    operator fun get(index: Int): T = elements[index]

    fun asSequence(): Sequence<T> = elements.asSequence()
}


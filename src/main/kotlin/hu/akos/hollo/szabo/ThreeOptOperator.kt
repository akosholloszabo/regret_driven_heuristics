package hu.akos.hollo.szabo

import hu.akos.hollo.szabo.utils.Permutation


class ThreeOptOperator {
    fun apply(
        permutation: Permutation,
        positions: IntArray,
    ) {
        require(positions.size == 3) { "Three positions are expected!" }
        require(positions[0] < positions[1] && positions[1] < positions[2]) {
            "Three positions should be ordered!"
        }

        val firstSection = permutation.slice(0 until positions[0])
        val secondSection = permutation.slice(positions[0] until positions[1])
        val thirdSection = permutation.slice(positions[1] until positions[2])
        val lastSection = permutation.slice(positions[2] until permutation.size)

        val newOrder = firstSection + thirdSection + secondSection + lastSection

        permutation.clear()
        newOrder.forEachIndexed { index, value -> permutation[index] = value }
    }

    fun revert(
        permutation: Permutation,
        positions: IntArray,
    ) {
        require(positions.size == 3) { "Three positions are expected!" }
        require(positions[0] < positions[1] && positions[1] < positions[2]) { "Three positions should be ordered!" }

        val middlePosition = positions[0] + positions[2] - positions[1]

        val firstSection = permutation.slice(0 until positions[0])
        val secondSection = permutation.slice(positions[0] until middlePosition)
        val thirdSection = permutation.slice(middlePosition until positions[2])
        val lastSection = permutation.slice(positions[2] until permutation.size)

        val newOrder = firstSection + thirdSection + secondSection + lastSection

        permutation.clear()
        newOrder.forEachIndexed { index, value -> permutation[index] = value }
    }
}
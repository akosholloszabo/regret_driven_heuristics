package hu.akos.hollo.szabo.heuristics

import hu.akos.hollo.szabo.model.get


fun applyThreeOptMove(
    permutation: IntArray,
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

    permutation.fill(-1)
    newOrder.forEachIndexed { index, value -> permutation[index] = value }
}

fun costDiffOfThreeOptMove(
    permutation: IntArray,
    positions: IntArray,
    weightMatrix: Array<DoubleArray>,
): Double {
    val weightRemoved = (0..<3)
        .sumOf { weightMatrix[permutation[positions[it] - 1], permutation[positions[it]]] }
    val weightAdded = (0..<3)
        .sumOf { weightMatrix[permutation[positions[it] - 1], permutation[positions[(it + 1) % 3]]] }
    return weightAdded - weightRemoved
}
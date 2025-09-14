package hu.akos.hollo.szabo.heuristics

import hu.akos.hollo.szabo.model.get

fun calcCostOfTspSolution(permutation: IntArray, distanceMatrix: Array<DoubleArray>): Double =
    (1 until permutation.size)
        .sumOf { index ->
            distanceMatrix[permutation[index - 1], permutation[index]]
        } +
            distanceMatrix[permutation.last(), permutation.size] +
            distanceMatrix[permutation.size, permutation[0]]
package hu.akos.hollo.szabo.model


data class RegretData(
    val file: String,
    val distanceMatrix: Array<DoubleArray>,
    val expectedRegretMatrix: Array<DoubleArray>,
    val predictedRegretMatrix: Array<DoubleArray>,
    val optCost: Double,
    val numberOfIterations: Int,
    val initialCost: Long,
    val bestCost: Long
)
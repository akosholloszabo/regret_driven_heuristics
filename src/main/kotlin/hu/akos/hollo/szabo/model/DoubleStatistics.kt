package hu.akos.hollo.szabo.model


data class DoubleStatistics(
    val max: Double,
    val min: Double,
    val avg: Double,
    val q1: Double,
    val q2: Double,
    val median: Double,
    val standardDeviation: Double,
)
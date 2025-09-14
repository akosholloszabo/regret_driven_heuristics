package hu.akos.hollo.szabo.model

data class CostRecord(
    val initialCost: Double,
    val builtCost: Double,
    val optimizedCost: Double,
    val optimal: Double,
)
package hu.akos.hollo.szabo

import hu.akos.hollo.szabo.utils.DoubleMatrix
import hu.akos.hollo.szabo.utils.asDoubleVector
import hu.akos.hollo.szabo.utils.asImmutable
import hu.akos.hollo.szabo.utils.toDoubleVector
import java.io.File


data class RegretData(
    val file: String,
    val distanceMatrix: DoubleMatrix,
    val expectedRegretMatrix: DoubleMatrix,
    val predictedRegretMatrix: DoubleMatrix,
    val optCost: Double,
    val numberOfIterations: Int,
    val initialCost: Long,
    val bestCost: Long
)

fun loadRegrets(sourceFolder: File): List<RegretData> {
    return sourceFolder.listFiles().asSequence()
        .map { file ->

            if (file.absolutePath.endsWith("results.json"))
                return@map null

            val distanceMatrix = file.useLines { lines ->
                lines
                    .drop(1)
                    .takeWhile { it.isNotBlank() }
                    .map { line ->
                        line
                            .split(" ")
                            .map { it.toDouble() }
                            .toDoubleArray()
                            .asDoubleVector()
                    }
                    .toList()
                    .toTypedArray()
                    .asImmutable()
                    .let { rows -> DoubleMatrix(rows) }
            }

            val expectedRegretMatrix = file.useLines { lines ->
                var wasStartLabel = false
                lines
                    .dropWhile {
                        wasStartLabel = wasStartLabel || it == "regret:"
                        it == "regret:" || !wasStartLabel
                    }
                    .takeWhile { it.isNotBlank() }
                    .map { line ->
                        line
                            .split(" ")
                            .map { it.toDouble() }
                            .toDoubleArray()
                            .asDoubleVector()
                    }
                    .toList()
                    .toTypedArray()
                    .asImmutable()
                    .let { rows -> DoubleMatrix(rows) }
            }

            val predictedRegretMatrix = file.useLines { lines ->
                var wasStartLabel = false
                lines
                    .dropWhile {
                        wasStartLabel = wasStartLabel || it == "regret_pred:"
                        it == "regret_pred:" || !wasStartLabel
                    }
                    .takeWhile { it.isNotBlank() }
                    .map { line ->
                        line
                            .split(" ")
                            .map { it.toDouble() }
                            .toDoubleArray()
                            .toDoubleVector()
                    }
                    .toList()
                    .toTypedArray()
                    .asImmutable()
                    .let { rows -> DoubleMatrix(rows) }
            }

            val optCost = file.useLines { lines ->
                lines.first { it.startsWith("opt_cost") }.split(" ")[1].toDouble()
            }

            val numberOfIterations = 0
//                file.useLines { lines ->
//                lines.first { it.startsWith("num_iterations") }.split(" ")[1].toInt()
//            }

            val initialCost = 0L
//                file.useLines { lines ->
//                    lines.first { it.startsWith("init_cost") }.split(" ")[1].toDouble().toLong()
//                }

            val bestCost = 0L
//                file.useLines { lines ->
//                    lines.first { it.startsWith("best_cost") }.split(" ")[1].toDouble().toLong()
//                }

            RegretData(
                file = file.absolutePath,
                distanceMatrix = distanceMatrix,
                expectedRegretMatrix = expectedRegretMatrix,
                predictedRegretMatrix = predictedRegretMatrix,
                optCost = optCost,
                numberOfIterations = numberOfIterations,
                initialCost = initialCost,
                bestCost = bestCost,
            )
        }
        .filterNotNull()
        .toList()
}
package hu.akos.hollo.szabo.model

data class Record(
    val noiseLevel: Double,
    val threeOptIteration: Int,
    val gapRatioStats: DoubleStatistics,
    val timeMillisStats: DoubleStatistics
)

fun parseDoubleStatistics(line: String): DoubleStatistics {
    val regex = Regex("""DoubleStatistics\(max=([-\d\.eE]+), min=([-\d\.eE]+), avg=([-\d\.eE]+), q1=([-\d\.eE]+), q2=([-\d\.eE]+), median=([-\d\.eE]+), standardDeviation=([-\d\.eE]+)\)""")
    val match = regex.find(line) ?: error("Invalid DoubleStatistics line: $line")
    val (max, min, avg, q1, q2, median, stdDev) = match.destructured
    return DoubleStatistics(
        max = max.toDouble(),
        min = min.toDouble(),
        avg = avg.toDouble(),
        q1 = q1.toDouble(),
        q2 = q2.toDouble(),
        median = median.toDouble(),
        standardDeviation = stdDev.toDouble()
    )
}

fun parseRecord(lines: List<String>): Record {
    val headerRegex = Regex("""NEW RUN: noiseLevel: ([\d\.]+), threeOptIteration: (\d+)""")
    val headerMatch = headerRegex.find(lines[0]) ?: error("Invalid header: ${lines[0]}")
    val (noiseLevel, threeOptIteration) = headerMatch.destructured
    val stats1 = parseDoubleStatistics(lines[1])
    val stats2 = parseDoubleStatistics(lines[2])
    return Record(
        noiseLevel = noiseLevel.toDouble(),
        threeOptIteration = threeOptIteration.toInt(),
        gapRatioStats = stats1,
        timeMillisStats = stats2
    )
}

fun loadRecords(path: String): List<Record> {
    val lines = java.io.File(path).readLines().filter { it.isNotBlank() }
    return lines.chunked(3).takeWhile { it.size == 3 }.map { parseRecord(it) }
}
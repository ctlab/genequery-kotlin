package gq.core.data


infix fun LongArray.intersectWithSorted(other: LongArray): LongArray {
    val result = mutableListOf<Long>()
    var thisIndex = 0
    var otherIndex = 0

    while (thisIndex < size && otherIndex < other.size) {
        if (this[thisIndex] == other[otherIndex]) {
            result.add(this[thisIndex]);
            thisIndex++;
            otherIndex++;
        } else if (this[thisIndex] > other[otherIndex]) {
            otherIndex++;
        } else {
            thisIndex++;
        }
    }
    return result.toLongArray()
}


fun LongArray.sizeOfIntersectionWithSorted(other: LongArray): Int {
    var result = 0
    var thisIndex = 0
    var otherIndex = 0

    while (thisIndex < size && otherIndex < other.size) {
        when {
            this[thisIndex] == other[otherIndex] -> {
                result++
                thisIndex++;
                otherIndex++;
            }
            this[thisIndex] > other[otherIndex] -> otherIndex++
            else -> thisIndex++
        }
    }
    return result
}


fun LongArray.sizeOfIntersectionWithSorted(other: List<Long>) = sizeOfIntersectionWithSorted(other.toLongArray())


open class GQModule(
        val datasetId: String,
        val clusterId: String,
        val species: Species,
        entrezIds: LongArray) {
    init {
        require(entrezIds.isNotEmpty()) {"Empty entrezIds array"}
    }

    val sortedEntrezIds = entrezIds.sorted().toLongArray()
    val size = sortedEntrezIds.size

    companion object {
        fun parseFullModuleName(fullName: String): Pair<String, String> {
            val parts = fullName.split('#')
            require(parts.size == 2) {"full module name $fullName has bad format"}
            return Pair(parts.component1(), parts.component2())
        }

        fun buildByFullName(fullName: String, species: Species, entrezIds: LongArray): GQModule {
            val (datasetId, clusterId) = parseFullModuleName(fullName)
            return GQModule(datasetId, clusterId, species, entrezIds)
        }

        fun joinFullName(datasetId: String, clusterId: String) = "$datasetId#$clusterId"
    }

    fun joinFullName() = joinFullName(datasetId, clusterId)

    fun fullName() = Pair(datasetId, clusterId)

    fun seriesName() = datasetId

    override fun toString() = "${joinFullName()}$species,$size genes)"
}
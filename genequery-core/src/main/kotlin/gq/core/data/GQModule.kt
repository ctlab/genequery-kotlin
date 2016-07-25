package gq.core.data


infix fun LongArray.intersectWithSorted(other: LongArray): LongArray {
    val result = mutableListOf<Long>()
    var thisIndex = 0
    var otherIndex = 0

    while (thisIndex < size && otherIndex < other.size) {
        if (this[thisIndex] == other[otherIndex]) {
            result.add(this[thisIndex])
            thisIndex++
            otherIndex++
        } else if (this[thisIndex] > other[otherIndex]) {
            otherIndex++
        } else {
            thisIndex++
        }
    }
    return result.toLongArray()
}


fun LongArray.sizeOfIntersectionWithSorted(other: LongArray): Int {
    var result = 0
    var thisIndex = 0
    var otherIndex = 0

    while (thisIndex < size && otherIndex < other.size) {
        if (this[thisIndex] == other[otherIndex]) {
            result++
            thisIndex++
            otherIndex++
        } else if (this[thisIndex] > other[otherIndex]) {
            otherIndex++
        } else {
            thisIndex++
        }
    }
    return result
}


fun LongArray.sizeOfIntersectionWithSorted(other: List<Long>) = sizeOfIntersectionWithSorted(other.toLongArray())


open class GQModule(val gse: Int, val gpl: Int, val number: Int, val species: Species, entrezIds: LongArray) {
    init {
        require(entrezIds.isNotEmpty(), { "Empty entrezIds array" })
    }

    val sortedEntrezIds = entrezIds.sorted().toLongArray()
    val size = sortedEntrezIds.size

    companion object {
        val GSE_PREFIX = "GSE"
        val GPL_PREFIX = "GPL"

        fun parseFullModuleName(fullName: String): Triple<Int, Int, Int> {
            val parts = fullName.split('_', '#')
            require(parts.size == 3, { "full module name $fullName has bad format" })
            require(parts.component1().startsWith(GSE_PREFIX))
            require(parts.component2().startsWith(GPL_PREFIX))
            return Triple(
                    parts.component1().substringAfter(GSE_PREFIX).toInt(),
                    parts.component2().substringAfter(GPL_PREFIX).toInt(),
                    parts.component3().toInt())
        }

        fun buildByFullName(fullName: String, species: Species, entrezIds: LongArray): GQModule {
            val (gse, gpl, number) = parseFullModuleName(fullName)
            return GQModule(gse, gpl, number, species, entrezIds)
        }
    }

    fun joinFullName() = "GSE${gse}_GPL$gpl#$number"

    fun fullName() = Triple(gse, gpl, number)

    fun seriesName() = Pair(gse, gpl)

    override fun toString() = "${joinFullName()}$species,$size genes)"
}
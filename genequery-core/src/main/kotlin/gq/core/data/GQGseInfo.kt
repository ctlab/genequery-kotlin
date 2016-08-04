package gq.core.data

import java.io.File

/**
 * id – numerical part of GSE name, i.e. 12345 for GSE12345
 */
data class GQGseInfo(val id: Int, val title: String) {
    companion object {
        const val DEFAULT_TITLE = "No title"

        fun parseIdFromName(name: String): Int {
            if (!name.startsWith("GSE")) {
                throw Exception("Wrong GSE name format: $name")
            }
            try {
                return name.removePrefix("GSE").toInt()
            } catch (e: NumberFormatException) {
                throw Exception("Wrong GSE name format: $name")
            }
        }

        fun idToName(id: Int) = "GSE" + id
    }
}


class GQGseInfoCollection(gqGseInfoItems: Iterable<GQGseInfo>) {
    constructor(gqGseInfoInit: () -> Iterable<GQGseInfo>) : this(gqGseInfoInit())

    private val idToInfo = gqGseInfoItems.associate { Pair(it.id, it) }

    operator fun get(name: String) = idToInfo[GQGseInfo.parseIdFromName(name)]

    operator fun get(id: Int) = idToInfo[id]

    fun size() = idToInfo.size
}


fun readGseInfoFromFile(path: String): List<GQGseInfo> {
    val infoList = mutableListOf<GQGseInfo>()
    File(path).forEachLine {
        if (it.isNotEmpty()) {
            try {
                val (name, title) = it.split("\t")
                infoList.add(GQGseInfo(GQGseInfo.parseIdFromName(name), title))
            } catch(e: Exception) {
                throw RuntimeException("Fail to parse GSE info: $it", e)
            }
        }
    }
    return infoList
}
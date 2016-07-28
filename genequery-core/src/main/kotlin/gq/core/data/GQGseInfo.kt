package gq.core.data

import java.io.File

data class GQGseInfo(val name: String, val title: String)


class GQGseInfoCollection(gqGseInfos: Iterable<GQGseInfo>) {
    constructor(gqGseInfoInit: () -> Iterable<GQGseInfo>) : this(gqGseInfoInit())

    private val nameToInfo = gqGseInfos.associate { Pair(it.name, it) }

    operator fun get(name: String) = nameToInfo[name]
    fun size() = nameToInfo.size
}


fun readGseInfoFromFile(path: String): List<GQGseInfo> {
    val infoList = mutableListOf<GQGseInfo>()
    File(path).forEachLine {
        if (it.isNotEmpty()) {
            try {
                val (name, title) = it.split("\t")
                infoList.add(GQGseInfo(name, title))
            } catch(e: Exception) {
                throw RuntimeException("Fail in parsing GSE info: $it", e)
            }
        }
    }
    return infoList
}
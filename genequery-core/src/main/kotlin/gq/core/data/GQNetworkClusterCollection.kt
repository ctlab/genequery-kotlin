package gq.core.data

import java.io.File

data class GQNetworkCluster(val id: Int,
                            val moduleNames: Set<String>,
                            val annotation: String? = null) {
    companion object {
        const val ZERO_CLUSTER_ID = 0
    }
}


class GQNetworkClusterCollection(clusters: Collection<GQNetworkCluster>) {
    val idToCluster = clusters.associate { Pair(it.id, it) }

    constructor(clustersFunc: () -> Collection<GQNetworkCluster>) : this(clustersFunc())
}


fun populateClusterInfoFromFiles(clusters: MutableCollection<GQNetworkCluster>,
                                 pathToClusters: String,
                                 pathToAnnotation: String) {
    val idToModuleNames = mutableMapOf<Int, Set<String>>()
    File(pathToClusters).forEachLine {
        if (it.isNotEmpty()) {
            try {
                val (clusterId, commaSeparatedModuleNames) = it.split("\t")
                idToModuleNames[clusterId.toInt()] = commaSeparatedModuleNames.split(",").toSet()
            } catch(e: Exception) {
                throw RuntimeException("Fail to parse line with cluster: $it", e)
            }
        }
    }

    val idToAnnotation = mutableMapOf<Int, String>()
    File(pathToAnnotation).forEachLine {
        if (it.isNotEmpty()) {
            try {
                val (clusterId, annotation) = it.split("\t")
                idToAnnotation[clusterId.toInt()] = annotation
            } catch(e: Exception) {
                throw RuntimeException("Fail to parse line with annotation: $it", e)
            }
        }
    }
    clusters.addAll(idToModuleNames.map { GQNetworkCluster(it.key, it.value, idToAnnotation[it.key]) }.toList())
}
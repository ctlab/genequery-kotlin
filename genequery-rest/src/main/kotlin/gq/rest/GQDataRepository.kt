package gq.rest

import gq.core.data.*
import gq.core.genes.*
import gq.rest.config.GQRestProperties
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Repository
class GQDataRepository @Autowired constructor(private val gqRestProperties: GQRestProperties) {
    companion object {
        val LOG = Logger.getLogger(GQDataRepository::class.java)
    }
    val toEntrezConverter = ToEntrezConverter().populate {
        LOG.info("Populate gene converter to-entrez from ${gqRestProperties.pathEnsemblToEntrez()}")
        File(gqRestProperties.pathEnsemblToEntrez()).readAndNormalizeGeneMappings(GeneFormat.ENSEMBL)
    }.populate {
        LOG.info("Populate gene converter to-entrez from ${gqRestProperties.pathRefseqToEntrez()}")
        File(gqRestProperties.pathRefseqToEntrez()).readAndNormalizeGeneMappings(GeneFormat.REFSEQ)
    }.populate {
        LOG.info("Populate gene converter to-entrez from ${gqRestProperties.pathSymbolToEntrez()}")
        File(gqRestProperties.pathSymbolToEntrez()).readAndNormalizeGeneMappings(GeneFormat.SYMBOL)
    }

    val fromEntrezToSymbolConverter = FromEntrezToSymbolConverter().populate {
        LOG.info("Populate gene converter from entrez to symbol from ${gqRestProperties.pathSymbolToEntrez()}")
        File(gqRestProperties.pathSymbolToEntrez()).readAndNormalizeGeneMappings(GeneFormat.SYMBOL)
    }


    val orthologyConverter = GeneOrthologyConverter {
        LOG.info("Initialize orthology converter from ${gqRestProperties.pathToOrthology()}")
        File(gqRestProperties.pathToOrthology()).readAndNormalizeGeneOrthologyMappings()
    }

    val smartConverter = SmartConverter(toEntrezConverter, fromEntrezToSymbolConverter, orthologyConverter)

    val moduleCollection = GQModuleCollection {
        val availableGmtFiles = Species.values()
                .map { Pair(it, gqRestProperties.pathToGMT(it)) }
                .filter { Files.exists(Paths.get(it.second)) }
        LOG.info("Initialize module collection from ${availableGmtFiles.joinToString(",")}")
        readModulesFromFiles(availableGmtFiles)
    }

    val gseInfoCollection = GQGseInfoCollection {
        LOG.info("Populate GSE info from ${gqRestProperties.pathGseInfo()}")
        readGseInfoFromFile(gqRestProperties.pathGseInfo())
    }

    val networkClusterCollection = GQNetworkClusterCollection {
        if (gqRestProperties.clusteringIsOn) {
            val clusters: MutableList<GQNetworkCluster> = mutableListOf()
            Species.values().forEach {
                val pathToClusters = gqRestProperties.pathToClusterModules(it)
                val pathToAnnotation = gqRestProperties.pathToClusterAnnotation(it)
                LOG.info("Reading for $it  cluster modules from $pathToClusters, annotation from $pathToAnnotation")
                populateClusterInfoFromFiles(
                        clusters,
                        pathToClusters,
                        pathToAnnotation)
            }
            LOG.info("${clusters.size} clusters read for all species.")
            clusters
        } else {
            LOG.info("Clustering is not available. See properties.")
            emptyList()
        }
    }
}
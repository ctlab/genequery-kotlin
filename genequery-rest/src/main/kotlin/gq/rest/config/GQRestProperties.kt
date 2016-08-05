package gq.rest.config

import gq.core.data.Species
import org.springframework.core.env.Environment
import java.nio.file.Paths

open class GQRestProperties(env: Environment) {
    var dataPath: String = env.getRequiredProperty("gq.rest.data.path")
    var adjPvalueMin: Double = env.getProperty("gq.rest.adjPvalueMin", "0.1").toDouble()
    var clusteringIsOn = env.getProperty("gq.rest.network.clustering.on", "false").toBoolean()

    fun pathToOrthology() = Paths.get(dataPath, "orthology.tsv").toString()
    fun pathRefseqToEntrez() = Paths.get(dataPath, "refseq-to-entrez.tsv").toString()
    fun pathEnsemblToEntrez() = Paths.get(dataPath, "ensembl-to-entrez.tsv").toString()
    fun pathSymbolToEntrez() = Paths.get(dataPath, "symbol-to-entrez.tsv").toString()
    fun pathGseInfo() = Paths.get(dataPath, "gse-info.txt").toString()

    fun pathToGMT(species: Species) = Paths.get(dataPath, "$species.modules.gmt").toString()
    fun pathToClusterModules(species: Species) = Paths.get(dataPath, "clusters", "${species}_clusters.txt").toString()
    fun pathToClusterAnnotation(species: Species) = Paths.get(dataPath, "clusters", "${species}_annotation.txt").toString()
}
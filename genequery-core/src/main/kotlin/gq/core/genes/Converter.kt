package gq.core.genes

import gq.core.data.Species
import java.io.File

data class GeneMapping(val species: Species, val entrezId: Long, val otherId: String)

abstract class FromGeneToGeneConverter<TFrom, TTo : Any, TCurrent : FromGeneToGeneConverter<TFrom, TTo, TCurrent>>(
        mappings: Iterable<GeneMapping> = emptyList()) {
    protected  val fromToMapping = hashMapOf<Species, Map<TFrom, TTo>>()

    init {
        populate(mappings)
    }

    abstract fun populate(initMappings: Iterable<GeneMapping>): TCurrent

    inline fun populate(initMappingsFunc: () -> Iterable<GeneMapping>) = populate(initMappingsFunc())

    operator fun get(species: Species, fromId: TFrom) = fromToMapping[species]!![fromId]

    fun convert(species: Species, fromId: TFrom) = get(species, fromId)
    fun convert(species: Species, fromIds: Iterable<TFrom>) = convertDetailed(species, fromIds).mapNotNull { it.value }.toSet()

    fun convertDetailed(species: Species, entrezIds: Iterable<TFrom>): Map<TFrom, TTo?> {
        val currentMapping = fromToMapping[species]!!
        return entrezIds.associate { Pair(it, currentMapping[it]) }
    }

}

class ToEntrezConverter(entrezOtherMappings: Iterable<GeneMapping> = emptyList())
: FromGeneToGeneConverter<String, Long, ToEntrezConverter>(entrezOtherMappings) {
    override fun populate(initMappings: Iterable<GeneMapping>): ToEntrezConverter {
        val newSpeciesToOtherToEntrez = initMappings
                .groupBy { it.species }
                .mapValues { it.value
                        .groupBy({ it.otherId }, { it.entrezId })
                        .mapValues { it.value.min()!! } }
        newSpeciesToOtherToEntrez
                .forEach { fromToMapping.merge(it.key, it.value, { existing, new -> existing + new }) }
        return this
    }

    fun normalizeAndConvert(species: Species,
                            geneIds: List<String>,
                            format: GeneFormat = GeneFormat.guess(geneIds)): Map<String, Long?> {
        if (format == GeneFormat.ENTREZ) return geneIds.associate { Pair(it, it.toLong()) }
        val originalToNorm = format.mapToNormalized(geneIds)
        val normToEntrez = convertDetailed(species, originalToNorm.values)
        return geneIds.associateBy({ it }, { normToEntrez[originalToNorm[it]] })
    }
}

class FromEntrezToSymbolConverter(entrezOtherMappings: Iterable<GeneMapping> = emptyList())
: FromGeneToGeneConverter<Long, String, FromEntrezToSymbolConverter>(entrezOtherMappings) {
    override fun populate(initMappings: Iterable<GeneMapping>): FromEntrezToSymbolConverter {
        val newSpeciesToEntrezToSymbol = initMappings
                .groupBy { it.species }
                .mapValues { it.value
                        .groupBy({ it.entrezId }, { it.otherId })
                        .mapValues { it.value.first() } }
        newSpeciesToEntrezToSymbol
                .forEach { fromToMapping.merge(it.key, it.value, { existing, new -> existing + new }) }
        return this
    }
}


data class OrthologyMapping(val groupId: Int,
                            val species: Species,
                            val entrezId: Long,
                            val symbolId: String,
                            val refseqId: String)

class GeneOrthologyConverter(orthologyMappings: Iterable<OrthologyMapping>) {
    private val groupIdToOrthology = orthologyMappings
            .groupBy { it.groupId }
            .mapValues { it.value.groupBy { it.species }.mapValues { it.value.single() } }
    private val entrezToOrthology = orthologyMappings.associate { Pair(it.entrezId, groupIdToOrthology[it.groupId]!!) }
    private val symbolToOrthology = orthologyMappings.associate { Pair(it.symbolId, groupIdToOrthology[it.groupId]!!) }
    private val refseqToOrthology = orthologyMappings.associate { Pair(it.refseqId, groupIdToOrthology[it.groupId]!!) }

    constructor(orthologyMappingsInit: () -> Iterable<OrthologyMapping>) : this(orthologyMappingsInit())

    fun getOrthologyByEntrez(entrezId: Long, speciesTo: Species) = entrezToOrthology[entrezId]?.get(speciesTo)
    fun getOrthologyBySymbol(symbolId: String, speciesTo: Species) = symbolToOrthology[symbolId]?.get(speciesTo)
    fun getOrthologyByRefseq(refseqId: String, speciesTo: Species) = refseqToOrthology[refseqId]?.get(speciesTo)

    fun getOrthologyByEntrez(entrezIds: Iterable<Long>, speciesTo: Species) =
            entrezIds.associate { Pair(it, getOrthologyByEntrez(it, speciesTo)) }
    fun getOrthologyBySymbol(symbolIds: Iterable<String>, speciesTo: Species) =
            symbolIds.associate { Pair(it, getOrthologyBySymbol(it, speciesTo)) }
    fun getOrthologyByRefseq(refseqIds: Iterable<String>, speciesTo: Species) =
            refseqIds.associate { Pair(it, getOrthologyByRefseq(it, speciesTo)) }
}


fun File.readGeneOrthologyMappings(): Iterable<OrthologyMapping> = readLines().mapNotNull {
    if (it.isNotEmpty()) {
        val (groupId, species, entrez, symbol, refseq) = it.split("\t")
        OrthologyMapping(
                groupId.toInt(),
                Species.fromOriginal(species),
                entrez.toLong(),
                GeneFormat.SYMBOL.normalize(symbol),
                GeneFormat.REFSEQ.normalize(refseq))
    } else {
        null
    }
}


fun File.readAndNormalizeGeneMappings(otherGeneFormat: GeneFormat): Iterable<GeneMapping> = readLines().mapNotNull {
    if (it.isNotEmpty()) {
        val (species, entrez, other) = it.split("\t")
        GeneMapping(
                Species.fromOriginal(species),
                entrez.toLong(),
                otherGeneFormat.normalize(other))
    } else {
        null
    }
}
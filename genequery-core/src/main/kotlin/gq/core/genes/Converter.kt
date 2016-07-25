package gq.core.genes

import gq.core.crossLinkMaps
import gq.core.data.Species
import java.io.File

data class GeneMapping(val species: Species, val entrezId: Long, val otherId: String)


fun File.readAndNormalizeGeneOrthologyMappings(): Iterable<OrthologyMapping> = readLines().mapNotNull {
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

abstract class FromGeneToGeneConverter<TFrom, TTo : Any, TSelf : FromGeneToGeneConverter<TFrom, TTo, TSelf>>(
        mappings: Iterable<GeneMapping> = emptyList()) {
    protected val fromToMapping = hashMapOf<Species, Map<TFrom, TTo>>()

    init {
        populate(mappings)
    }

    abstract fun populate(initMappings: Iterable<GeneMapping>): TSelf

    inline fun populate(initMappingsFunc: () -> Iterable<GeneMapping>) = populate(initMappingsFunc())

    operator fun get(species: Species, fromId: TFrom) = fromToMapping[species]!![fromId]

    fun convert(species: Species, fromId: TFrom) = get(species, fromId)
    fun convert(species: Species, fromIds: Iterable<TFrom>) = convertDetailed(species, fromIds).values.filterNotNull().toSet()

    fun convertDetailed(species: Species, geneIds: Iterable<TFrom>): Map<TFrom, TTo?> {
        val currentMapping = fromToMapping[species]!!
        return geneIds.associate { Pair(it, currentMapping[it]) }
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

    /**
     * Normalize genes of *any* format and convert them to *entrez*.
     */
    fun normalizeAndConvert(species: Species,
                            geneIds: List<String>,
                            format: GeneFormat = GeneFormat.guess(geneIds)): Map<String, Long?> {
        if (format == GeneFormat.ENTREZ) return geneIds.associate { Pair(it, it.toLong()) }
        val originalToNorm = format.mapToNormalized(geneIds)
        val normToEntrez = convertDetailed(species, originalToNorm.values)
        return crossLinkMaps(originalToNorm, normToEntrez)
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


class GeneOrthologyConverter(orthologyMappings: Iterable<OrthologyMapping>) {
    private val groupIdToOrthology = orthologyMappings
            .groupBy { it.groupId }
            .mapValues { it.value.groupBy { it.species }.mapValues { it.value.minBy { it.entrezId } } }
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

    fun getOrthology(geneIds: List<String>, speciesTo: Species, format: GeneFormat = GeneFormat.guess(geneIds)) =
            when (format) {
            // TODO create SubEnum for this purpose
                GeneFormat.ENSEMBL ->
                    throw IllegalArgumentException("Orthology mapping contains SYMBOL, ENTREZ and REFSEQ formats only. ENSEMBL passed.")
                GeneFormat.ENTREZ -> {
                    val strToLongEntrez = geneIds.associate { Pair(it, it.toLong()) }
                    crossLinkMaps(strToLongEntrez, getOrthologyByEntrez(strToLongEntrez.values, speciesTo))
                }
                GeneFormat.REFSEQ -> getOrthologyByRefseq(geneIds, speciesTo)
                GeneFormat.SYMBOL -> getOrthologyBySymbol(geneIds, speciesTo)
            }
}


class SmartConverter(private val toEntrezConverter: ToEntrezConverter,
                     private val fromEntrezToSymbolConverter: FromEntrezToSymbolConverter,
                     private val orthologyConverter: GeneOrthologyConverter) {

    fun toEntrez(geneIds: List<String>,
                 formatFrom: GeneFormat,
                 speciesFrom: Species,
                 speciesTo: Species = speciesFrom): Map<String, Long?> {
        if (geneIds.isEmpty()) return emptyMap()
        if (speciesTo == speciesFrom) return toEntrezConverter.normalizeAndConvert(speciesFrom, geneIds, formatFrom)
        return when (formatFrom) {
            GeneFormat.ENSEMBL, GeneFormat.REFSEQ -> {
                val ensemblToOriginalEntrez = toEntrezConverter.normalizeAndConvert(speciesFrom, geneIds, formatFrom)
                val entrezOriginalToTarget = orthologyConverter.getOrthologyByEntrez(
                        ensemblToOriginalEntrez.values.filterNotNull(), speciesTo).mapValues { it.value?.entrezId }
                crossLinkMaps(ensemblToOriginalEntrez, entrezOriginalToTarget)
            }
            GeneFormat.ENTREZ -> {
                val strToLongEntrez = geneIds.associate { Pair(it, it.toLong()) }
                val longEntrezToTargetEntrez = toEntrez(
                        strToLongEntrez.values.toList(),
                        speciesFrom,
                        speciesTo)
                crossLinkMaps(strToLongEntrez, longEntrezToTargetEntrez)
            }
            GeneFormat.SYMBOL -> orthologyConverter.getOrthologyBySymbol(geneIds, speciesTo).mapValues { it.value?.entrezId }
        }
    }

    fun toEntrez(entrezIds: List<Long>,
                 speciesFrom: Species,
                 speciesTo: Species = speciesFrom): Map<Long, Long?> {
        if (entrezIds.isEmpty()) return emptyMap()
        if (speciesTo == speciesFrom) return entrezIds.associate { Pair(it, it) }
        return orthologyConverter.getOrthologyByEntrez(entrezIds, speciesTo).mapValues { it.value?.entrezId }
    }

    fun toSymbol(geneIds: List<String>,
                 formatFrom: GeneFormat = GeneFormat.guess(geneIds),
                 speciesFrom: Species,
                 speciesTo: Species = speciesFrom): Map<String, String?> {
        if (geneIds.isEmpty()) return emptyMap()

        if (speciesTo == speciesFrom) {
            if (formatFrom == GeneFormat.SYMBOL) return geneIds.associate { Pair(it, GeneFormat.SYMBOL.normalize(it)) }
            val originalToEntrezIds = toEntrezConverter.normalizeAndConvert(speciesFrom, geneIds, formatFrom)
            val entrezToSymbol = fromEntrezToSymbolConverter.convertDetailed(
                    speciesFrom, originalToEntrezIds.values.filterNotNull())
            return crossLinkMaps(originalToEntrezIds, entrezToSymbol)
        }

        return when (formatFrom) {
            GeneFormat.ENTREZ -> {
                val strToLongEntrez = geneIds.associate { Pair(it, it.toLong()) }
                crossLinkMaps(strToLongEntrez, toSymbol(strToLongEntrez.values.toList(), speciesFrom, speciesTo))
            }
            else -> {
                val normalizedIds = formatFrom.mapToNormalized(geneIds).values.toList()
                val normalizedToOrthology = when (formatFrom) {
                    GeneFormat.ENSEMBL, GeneFormat.REFSEQ -> {
                        val normalizedToEntrez = toEntrezConverter.convertDetailed(speciesFrom, normalizedIds)
                        crossLinkMaps(
                                normalizedToEntrez,
                                orthologyConverter.getOrthologyByEntrez(normalizedToEntrez.values.filterNotNull(), speciesTo))
                    }
                    else -> orthologyConverter.getOrthology(normalizedIds, speciesTo, formatFrom)
                }
                geneIds.associate { Pair(it, formatFrom.normalize(it)) }.mapValues { normalizedToOrthology[it.value]?.symbolId }
            }
        }
    }

    fun toSymbol(entrezIds: List<Long>,
                 speciesFrom: Species,
                 speciesTo: Species = speciesFrom): Map<Long, String?> {
        if (entrezIds.isEmpty()) return emptyMap()
        if (speciesFrom == speciesTo) return fromEntrezToSymbolConverter.convertDetailed(speciesFrom, entrezIds)
        return orthologyConverter.getOrthologyByEntrez(entrezIds, speciesTo).mapValues { it.value?.symbolId }
    }
}
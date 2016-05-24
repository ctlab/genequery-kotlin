package gq.core.genes

import gq.core.data.Species
import java.io.File

data class GeneMapping(val species: Species, val entrezId: Long, val otherId: String)

class GeneConverter(toEntrezFromOtherMappings: Iterable<GeneMapping> = emptyList(),
                    toEntrezFromSymbolMappings: Iterable<GeneMapping> = emptyList()) {
    private val speciesToEntrezToSymbol = hashMapOf<Species, Map<Long, String>>()
    private val speciesToOtherToEntrez = hashMapOf<Species, Map<String, Long>>()

    init {
        populateOtherToEntrez(toEntrezFromOtherMappings)
        populateEntrezToSymbol(toEntrezFromSymbolMappings)
    }

    inline fun populateOtherToEntrez(speciesEntrezOtherInit: () -> Iterable<GeneMapping>) =
            populateOtherToEntrez(speciesEntrezOtherInit())

    fun populateOtherToEntrez(speciesEntrezOther: Iterable<GeneMapping>): GeneConverter {
        val newSpeciesToOtherToEntrez = speciesEntrezOther
                .groupBy { it.species }
                .mapValues { it.value
                        .groupBy({ it.otherId }, { it.entrezId })
                        .mapValues { it.value.min()!! } }
        newSpeciesToOtherToEntrez
                .forEach { speciesToOtherToEntrez.merge(it.key, it.value, { existing, new -> existing + new }) }
        return this
    }

    inline fun populateEntrezToSymbol(speciesEntrezSymbolInit: () -> Iterable<GeneMapping>) =
            populateEntrezToSymbol(speciesEntrezSymbolInit())

    fun populateEntrezToSymbol(speciesEntrezSymbol: Iterable<GeneMapping>): GeneConverter {
        val newSpeciesToEntrezToSymbol = speciesEntrezSymbol
                .groupBy { it.species }
                .mapValues { it.value
                        .groupBy({ it.entrezId }, { it.otherId })
                        .mapValues { it.value.first() } }
        newSpeciesToEntrezToSymbol
                .forEach { speciesToEntrezToSymbol.merge(it.key, it.value, { existing, new -> existing + new }) }
        return this
    }

    operator fun get(species: Species, entrezId: Long) = speciesToEntrezToSymbol[species]!![entrezId]
    operator fun get(species: Species, otherId: String) = speciesToOtherToEntrez[species]!![otherId]

    fun entrezToSymbol(species: Species, entrezIds: Iterable<Long>) = entrezToSymbolDetailed(species, entrezIds)
            .mapNotNull { it.value }.toSet()
    fun otherToEntrez(species: Species, otherIds: Iterable<String>) = otherToEntrezDetailed(species, otherIds)
            .mapNotNull { it.value }.toSet()

    fun entrezToSymbolDetailed(species: Species, entrezIds: Iterable<Long>): Map<Long, String?> {
        val currentMapping = speciesToEntrezToSymbol[species]!!
        return entrezIds.associate { Pair(it, currentMapping[it]) }
    }

    fun otherToEntrezDetailed(species: Species, entrezIds: Iterable<String>): Map<String, Long?> {
        val currentMapping = speciesToOtherToEntrez[species]!!
        return entrezIds.associate { Pair(it, currentMapping[it]) }
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

    constructor(orthologyMappingsInit: () -> Iterable<OrthologyMapping>) : this(orthologyMappingsInit())

    operator fun get(entrezId: Long, species: Species) = entrezToOrthology[entrezId]?.get(species)
    operator fun get(symbolId: String, species: Species) = symbolToOrthology[symbolId]?.get(species)

    fun entrezToEntrezDetailed(entrezIds: Iterable<Long>, species: Species) =
            entrezIds.associate { Pair(it, this[it, species]?.entrezId) }

    fun entrezToSymbolDetailed(entrezIds: Iterable<Long>, species: Species) =
            entrezIds.associate { Pair(it, this[it, species]?.symbolId) }

    fun symbolToEntrezDetailed(symbolIds: Iterable<String>, species: Species) =
            symbolIds.associate { Pair(it, this[it, species]?.entrezId) }

    fun symbolToSymbolDetailed(symbolIds: Iterable<String>, species: Species) =
            symbolIds.associate { Pair(it, this[it, species]?.symbolId) }

    fun bulkEntrezToEntrez(entrezIds: Iterable<Long>,
                           speciesTo: Species) = entrezToEntrezDetailed(entrezIds, speciesTo).values.mapNotNull { it }
    fun bulkEntrezToSymbol(entrezIds: Iterable<Long>,
                           speciesTo: Species) = entrezToSymbolDetailed(entrezIds, speciesTo).values.mapNotNull { it }
    fun bulkSymbolToEntrez(symbolIds: Iterable<String>,
                           speciesTo: Species) = symbolToEntrezDetailed(symbolIds, speciesTo).values.mapNotNull { it }
    fun bulkSymbolToSymbol(symbolIds: Iterable<String>,
                           speciesTo: Species) = symbolToSymbolDetailed(symbolIds, speciesTo).values.mapNotNull { it }
}


fun File.readGeneOrthologyMappings(): Iterable<OrthologyMapping> = readLines().mapNotNull {
    if (it.isNotEmpty()) {
        val (groupId, species, entrez, symbol, refseq) = it.split("\t")
        OrthologyMapping(groupId.toInt(), Species.fromOriginal(species), entrez.toLong(), symbol, refseq)
    } else {
        null
    }
}


fun File.readGeneMappings(): Iterable<GeneMapping> = readLines().mapNotNull {
    if (it.isNotEmpty()) {
        val (species, entrez, other) = it.split("\t")
        GeneMapping(Species.fromOriginal(species), entrez.toLong(), other)
    } else {
        null
    }
}

fun detectGenesFormat(genes: List<String>): GeneFormat {
    require(genes.isNotEmpty(), { "Gene list is empty." })

    val actualGeneFormat = guessGeneFormat(genes.first())
    genes.forEachIndexed { i, gene ->
        val currentGeneFormat = guessGeneFormat(gene)
        if (currentGeneFormat != actualGeneFormat) throw IllegalArgumentException(
                "Ambiguous gene format: first gene (${genes.first()}) is $actualGeneFormat, but ${i + 1}th gene ($gene) is $currentGeneFormat")
    }
    return actualGeneFormat
}
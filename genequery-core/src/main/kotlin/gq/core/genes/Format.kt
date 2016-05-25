package gq.core.genes

private fun isDigitsAndNotLeadingDot(string: String) = GeneFormat.digitsAndDotRegex.matches(string)


private fun isPrefixAndDigitsWithMaybeDot(string: String, prefix: String) =
        string.startsWith(prefix) && isDigitsAndNotLeadingDot(string.substringAfter(prefix))


enum class GeneFormat(val formatName: String) {
    ENTREZ("entrez") {
        override fun isFormatOf(gene: String): Boolean = try { isFormatOf(gene.toLong()) } catch (e: NumberFormatException) { false }
        fun isFormatOf(gene: Long): Boolean = gene >= 0
        override fun normalize(gene: String) = gene
    },
    REFSEQ("refseq") {
        override fun isFormatOf(gene: String): Boolean = GeneFormat.REFSEQ_PREFIXES.any { isPrefixAndDigitsWithMaybeDot(gene, it) }
        override fun normalize(gene: String) = gene.toUpperCase().substringBefore(".")
    },
    ENSEMBL("ensembl") {
        override fun isFormatOf(gene: String): Boolean = GeneFormat.ENSEMBL_PREFIXES.any { isPrefixAndDigitsWithMaybeDot(gene, it) }
        override fun normalize(gene: String) = gene.toUpperCase().substringBefore(".")
    },
    SYMBOL("symbol") {
        override fun isFormatOf(gene: String): Boolean = listOf(ENTREZ, REFSEQ, ENSEMBL).none { it.isFormatOf(gene) }
        override fun normalize(gene: String) = gene.toUpperCase()
    };

    companion object {
        internal val REFSEQ_PREFIXES = arrayOf("NM_", "NR_", "XM_", "XR_")
        internal val ENSEMBL_PREFIXES = arrayOf("ENSG", "ENSRNOG", "ENSMUSG")
        internal val digitsAndDotRegex = Regex("\\d+(\\.?\\d+)?")

        fun guess(gene: String): GeneFormat {
            require(gene.isNotBlank(), { "Empty string passed." })
            values().forEach { if (it.isFormatOf(gene)) return it }
            return SYMBOL
        }

        fun guess(genes: List<String>): GeneFormat {
            require(genes.isNotEmpty(), { "Gene list is empty." })

            val actualGeneFormat = guess(genes.first())
            genes.forEachIndexed { i, gene ->
                val currentGeneFormat = guess(gene)
                if (currentGeneFormat != actualGeneFormat)
                    throw IllegalArgumentException(
                            "Ambiguous gene format: first gene (${genes.first()}) is $actualGeneFormat, but ${i + 1}th gene ($gene) is $currentGeneFormat")
            }
            return actualGeneFormat
        }
    }

    abstract fun normalize(gene: String): String

    abstract fun isFormatOf(gene: String): Boolean

    fun require(gene: String) = require(isFormatOf(gene), { "Format of gene $gene is not $this" })

    fun mapToNormalized(genes: Iterable<String>) = genes.associate { Pair(it, normalize(it)) }

    override fun toString() = formatName
}
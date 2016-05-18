package gq.core.genes

fun String.isDigitsAndNotLeadingDot(): Boolean {
    if (this[0] == '.') return false
    return (try { toDouble() } catch (e: NumberFormatException) { -1.0 }) >= 0
}

private fun String.isPrefixAndDigitsWithMaybeDot(prefix: String): Boolean {
    return startsWith(prefix) && substringAfter(prefix).isDigitsAndNotLeadingDot()
}

enum class GeneFormat(val format: String) {
    ENTREZ("entrez"),
    REFSEQ("refseq"),
    ENSEMBL("ensembl"),
    SYMBOL("symbol");

    companion object {
        internal val REFSEQ_PREFIXES = arrayOf("NM_", "NR_", "XM_", "XR_")
        internal val ENSEMBL_PREFIXES = arrayOf("ENSG", "ENSRNOG", "ENSMUSG")
    }

    override fun toString() = format
}

fun Long.isEntrez() = this >= 0
fun String.isEntrez() = try { toLong() } catch (e: NumberFormatException) { Long.MIN_VALUE }.isEntrez()
fun String.isRefSeq() = GeneFormat.REFSEQ_PREFIXES.any { isPrefixAndDigitsWithMaybeDot(it) }
fun String.isEnsembl() = GeneFormat.ENSEMBL_PREFIXES.any { isPrefixAndDigitsWithMaybeDot(it) }

/**
 * Returns true if gene is none of entrez, refseq or ensemble format.
 */
fun String.isSymbol() = !(isEntrez() || isRefSeq() || isEnsembl())

fun String.guessGeneFormat(): GeneFormat {
    if (isEntrez()) return GeneFormat.ENTREZ
    if (isRefSeq()) return GeneFormat.REFSEQ
    if (isEnsembl()) return GeneFormat.ENSEMBL
    return GeneFormat.SYMBOL
}
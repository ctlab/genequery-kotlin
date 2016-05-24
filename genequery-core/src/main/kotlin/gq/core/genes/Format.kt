package gq.core.genes

private fun isDigitsAndNotLeadingDot(string: String) = GeneFormat.digitsAndDotRegex.matches(string)


private fun isPrefixAndDigitsWithMaybeDot(string: String, prefix: String) =
        string.startsWith(prefix) && isDigitsAndNotLeadingDot(string.substringAfter(prefix))


enum class GeneFormat(val format: String) {
    ENTREZ("entrez"),
    REFSEQ("refseq"),
    ENSEMBL("ensembl"),
    SYMBOL("symbol");

    companion object {
        internal val REFSEQ_PREFIXES = arrayOf("NM_", "NR_", "XM_", "XR_")
        internal val ENSEMBL_PREFIXES = arrayOf("ENSG", "ENSRNOG", "ENSMUSG")
        internal val digitsAndDotRegex = Regex("\\d+(\\.?\\d+)?")
    }

    override fun toString() = format
}


fun isEntrez(number: Long) = number >= 0
fun isEntrez(string: String) = try { isEntrez(string.toLong()) } catch (e: NumberFormatException) { false }
fun isRefSeq(string: String) = GeneFormat.REFSEQ_PREFIXES.any { isPrefixAndDigitsWithMaybeDot(string, it) }
fun isEnsembl(string: String) = GeneFormat.ENSEMBL_PREFIXES.any { isPrefixAndDigitsWithMaybeDot(string, it) }
fun isSymbol(string: String) = !(isEntrez(string) || isRefSeq(string) || isEnsembl(string))


fun guessGeneFormat(string: String): GeneFormat {
    require(string.isNotBlank(), { "Empty string passed." })
    if (isEntrez(string)) return GeneFormat.ENTREZ
    if (isRefSeq(string)) return GeneFormat.REFSEQ
    if (isEnsembl(string)) return GeneFormat.ENSEMBL
    return GeneFormat.SYMBOL
}
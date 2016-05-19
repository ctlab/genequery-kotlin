package gq.core.data

enum class Species(val original: String) {
    HUMAN("hs"),
    MOUSE("mm"),
    RAT("rt");

    companion object {
        fun fromOriginal(original: String): Species {
            values().forEach { if (it.original == original) return it }
            throw IllegalArgumentException("No enum for string $original")
        }
    }

    override fun toString(): String = original
}
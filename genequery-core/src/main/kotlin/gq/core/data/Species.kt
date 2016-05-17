package gq.core.data

enum class Species(val original: String) {
    HUMAN("hs"),
    MOUSE("mm"),
    RAT("rt");

    override fun toString(): String = original
}
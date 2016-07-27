package gq.core.math

/**
 * TODO
 */
class FisherExact private constructor(universeSize: Int = 7000) {
    companion object {
        val EPS = 1e-323
        val instance = FisherExact()
    }

    private val logFactorials = DoubleArray(universeSize + 1)

    init {
        logFactorials[0] = 0.0
        for (i in 1..logFactorials.lastIndex) {
            logFactorials[i] = logFactorials[i - 1] + Math.log(i.toDouble())
        }
    }

    fun rightTailPvalue(a: Int, b: Int, c: Int, d: Int): Double {
        require(a >= 0 && b >= 0 && c >= 0 && d >= 0, {
            "Every argument must be non-negative: a=$a, b=$b, c=$c, d=$d"
        })
        require(a + b + c + d < logFactorials.size, {
            "Sum of the arguments must be not greater than universe: $a + $b + $c + $d > ${logFactorials.size - 1}"
        })
        // TODO how to bypass reassign?
        var aa = a
        var bb = b
        var cc = c
        var dd = d
        var pSum = 0.0
        var p = calculateHypergeomP(aa, bb, cc, dd)
        while (cc >= 0 && bb >= 0) {
            pSum += p
            if (bb == 0 || cc == 0) break
            ++aa
            --bb
            --cc
            ++dd
            p = calculateHypergeomP(aa, bb, cc, dd)
        }
        return pSum
    }

    private fun calculateHypergeomP(a: Int, b: Int, c: Int, d: Int): Double {
        return Math.exp(
                logFactorials[a + b]
                        + logFactorials[c + d]
                        + logFactorials[a + c]
                        + logFactorials[b + d]
                        - logFactorials[a + b + c + d]
                        - logFactorials[a]
                        - logFactorials[b]
                        - logFactorials[c]
                        - logFactorials[d])
    }
}
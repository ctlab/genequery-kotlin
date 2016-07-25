package gq.core.math

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by Arbuzov Ivan.
 */
class FisherExactKtTest {
    @Test
    fun testRightTailBasic() {
        assertEquals(5.8689429962759094E-24, FisherExact.instance.rightTailPvalue(16, 81, 11, 6892), FisherExact.EPS)
        assertEquals(2.3879574661733384E-28, FisherExact.instance.rightTailPvalue(16, 76, 3, 6805), FisherExact.EPS)
        assertEquals(1.909706400598598E-15, FisherExact.instance.rightTailPvalue(16, 80, 59, 6845), FisherExact.EPS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRightTailError() {
        assertEquals(1.909706400598598E-15, FisherExact.instance.rightTailPvalue(16, 80, 59, 6846), FisherExact.EPS)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRightTailNegative() {
        assertEquals(1.909706400598598E-15, FisherExact.instance.rightTailPvalue(-1, 80, 59, 6846), FisherExact.EPS)
    }

}
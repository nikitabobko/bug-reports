package bobko.todomail.settings

import org.junit.Assert
import org.junit.Test

private const val itemHeight = 50

/**
 * Test for [bobko.todomail.settings.calculateIndexOffset]
 */
class CalculateIndexOffsetTest {
    @Test
    fun test() {
        for (i in 0 until 100) {
            for (j in openRange(-itemHeight / 2, itemHeight / 2)) {
                doTest(i * itemHeight + j, i)
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun openRange(left: Int, right: Int) = left + 1 until right

    private fun doTest(pixelOffset: Int, expectedIndexOffset: Int) {
        Assert.assertEquals(
            "pixelOffset=$pixelOffset",
            expectedIndexOffset,
            calculateIndexOffset(pixelOffset, itemHeight)
        )
        Assert.assertEquals(
            "pixelOffset=$pixelOffset",
            -expectedIndexOffset,
            calculateIndexOffset(-pixelOffset, itemHeight)
        )
    }
}

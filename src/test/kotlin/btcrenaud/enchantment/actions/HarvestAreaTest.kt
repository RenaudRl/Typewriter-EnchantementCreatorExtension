package btcrenaud.enchantment.actions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HarvestAreaTest {
    @Test
    fun `radius 1 covers a 3x3 square including the center`() {
        val offsets = harvestOffsets(1)

        assertEquals(9, offsets.size)
        assertTrue((0 to 0) in offsets)
        assertTrue((-1 to 1) in offsets)
    }

    @Test
    fun `radius 0 or below harvests only the center`() {
        assertEquals(listOf(0 to 0), harvestOffsets(0))
        assertEquals(listOf(0 to 0), harvestOffsets(-3))
    }

    @Test
    fun `merged drops are split into full stacks then the remainder`() {
        assertEquals(listOf(64, 64, 10), stackSizes(138, 64))
        assertEquals(listOf(16, 16), stackSizes(32, 16))
        assertEquals(listOf(3), stackSizes(3, 64))
    }

    @Test
    fun `nothing to drop gives no stack`() {
        assertEquals(emptyList(), stackSizes(0, 64))
        assertEquals(emptyList(), stackSizes(5, 0))
    }
}

package btcrenaud.enchantment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EnchantmentRuntimeTest {
    @Test
    fun `bounds protect registry and item operations`() {
        assertEquals(1, EnchantmentRuntime.clampLevel(Int.MIN_VALUE, 0))
        assertEquals(255, EnchantmentRuntime.clampLevel(Int.MAX_VALUE, Int.MAX_VALUE))
        assertEquals(100, EnchantmentRuntime.clampChance(999))
        assertEquals(0, EnchantmentRuntime.clampWeight(-1))
    }

    @Test
    fun `level modes are deterministic`() {
        assertTrue(EnchantmentRuntime.matchesLevel(EnchantmentLevelMode.EXACT, 3, 3))
        assertFalse(EnchantmentRuntime.matchesLevel(EnchantmentLevelMode.EXACT, 3, 4))
        assertTrue(EnchantmentRuntime.matchesLevel(EnchantmentLevelMode.AT_LEAST, 3, 4))
        assertTrue(EnchantmentRuntime.matchesLevel(EnchantmentLevelMode.AT_MOST, 3, 2))
        assertTrue(EnchantmentRuntime.matchesLevel(EnchantmentLevelMode.EXACT, 0, 255))
    }

    @Test
    fun `slot groups match their concrete equipment slots`() {
        assertTrue(EnchantmentRuntime.slotMatches(EnchantSlot.HAND, EnchantSlot.OFFHAND))
        assertTrue(EnchantmentRuntime.slotMatches(EnchantSlot.ARMOR, EnchantSlot.CHEST))
        assertFalse(EnchantmentRuntime.slotMatches(EnchantSlot.ARMOR, EnchantSlot.MAINHAND))
        assertTrue(EnchantmentRuntime.slotMatches(EnchantSlot.ANY, EnchantSlot.FEET))
    }
}

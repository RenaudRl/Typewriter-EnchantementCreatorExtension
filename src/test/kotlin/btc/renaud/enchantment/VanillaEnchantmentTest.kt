package btc.renaud.enchantment

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.bukkit.enchantments.Enchantment
import org.mockito.Mockito.mock

class VanillaEnchantmentTest {
    private val defaultResolver = vanillaEnchantmentResolver

    @AfterTest
    fun tearDown() {
        vanillaEnchantmentResolver = defaultResolver
    }

    @Test
    fun `all vanilla enchantments resolve`() {
        vanillaEnchantmentResolver = { mock(Enchantment::class.java) }

        VanillaEnchantment.entries.forEach { enchantment ->
            assertNotNull(enchantment.resolve())
        }
    }
}


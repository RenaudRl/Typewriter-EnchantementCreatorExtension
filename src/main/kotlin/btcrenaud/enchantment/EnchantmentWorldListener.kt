package btcrenaud.enchantment

import com.typewritermc.core.extension.Initializable
import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.engine.paper.plugin
import lirand.api.extensions.server.registerEvents
import org.bukkit.Material
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.VillagerAcquireTradeEvent
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

/** Emerald price bounds for librarian trades selling custom enchantment books. */
private const val TRADE_BASE_PRICE = 12
private const val TRADE_PRICE_PER_LEVEL = 6
private const val TRADE_MAX_USES = 3

/**
 * Injects custom enchantments into the world: enchanted books in generated
 * loot chests ([RegisteredEnchantment.treasureChance]) and librarian trades
 * ([RegisteredEnchantment.tradeChance]).
 */
@Singleton
object EnchantmentWorldListener : Initializable, Listener {

    override suspend fun initialize() {
        plugin.registerEvents(this)
    }

    override suspend fun shutdown() {
        HandlerList.unregisterAll(this)
    }

    @EventHandler(ignoreCancelled = true)
    fun onLootGenerate(event: LootGenerateEvent) {
        for (def in EnchantmentManager.definitions()) {
            if (def.treasureChance <= 0) continue
            if (Random.nextInt(100) >= def.treasureChance.coerceAtMost(100)) continue
            val level = Random.nextInt(def.maxLevel.coerceAtLeast(1)) + 1
            val book = EnchantmentManager.buildBook(def, level) ?: continue
            event.loot.add(book)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onVillagerAcquireTrade(event: VillagerAcquireTradeEvent) {
        val villager = event.entity as? Villager ?: return
        if (villager.profession != Villager.Profession.LIBRARIAN) return
        if (event.recipe.result.type != Material.ENCHANTED_BOOK) return

        val candidates = EnchantmentManager.definitions().filter { it.tradeChance > 0 }
        if (candidates.isEmpty()) return
        val def = candidates.random()
        if (Random.nextInt(100) >= def.tradeChance.coerceAtMost(100)) return

        val level = Random.nextInt(def.maxLevel.coerceAtLeast(1)) + 1
        val book = EnchantmentManager.buildBook(def, level) ?: return

        val price = (TRADE_BASE_PRICE + TRADE_PRICE_PER_LEVEL * level).coerceAtMost(64)
        val recipe = MerchantRecipe(book, TRADE_MAX_USES)
        recipe.addIngredient(ItemStack(Material.EMERALD, price))
        recipe.addIngredient(ItemStack(Material.BOOK))
        event.recipe = recipe
    }
}

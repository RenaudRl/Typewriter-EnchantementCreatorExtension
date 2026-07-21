package btcrenaud.enchantment

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.core.extension.annotations.ContentEditor
import com.typewritermc.core.extension.annotations.MaterialProperties
import com.typewritermc.core.extension.annotations.MaterialProperty
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.ManifestEntry
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.content.modes.custom.HoldingItemContentMode
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.Material
import java.time.Duration

@Entry(
    name = "enchantment_definition",
    description = "Defines a custom enchantment",
    color = Colors.PURPLE,
    icon = "material-symbols:auto-fix"
)
@Tags("enchantment_definition")
class EnchantmentDefinition(
    override val id: String = "",
    override val name: String = "",
    @Help("Display name of the enchantment")
    override val displayName: String = "",
    @Help("Lore shown on items for this enchantment")
    override val enchantmentLore: String = "",
    @Help("Color of the enchantment name on items (MiniMessage tag, e.g. 'gray' or '#aaaaaa')")
    override val nameColor: String = "gray",
    @Help("Items that support this enchantment")
    @MaterialProperties(MaterialProperty.ITEM)
    @ContentEditor(HoldingItemContentMode::class)
    override val supportedItems: List<Material> = emptyList(),
    @Help("Cost in an anvil to combine")
    override val anvilCost: Int = 1,
    @Help("Maximum level of the enchantment")
    override val maxLevel: Int = 1,
    @Help("Weight used for random selection")
    override val weight: Int = 1,
    @Help("Minimum cost when enchanting")
    override val minimumCost: Cost = Cost(),
    @Help("Maximum cost when enchanting")
    override val maximumCost: Cost = Cost(),
    @Help("Equipment slots the enchantment is active in (empty = armor + hands)")
    override val activeSlots: List<EnchantSlot> = emptyList(),
    @Help("Enchantments that cannot coexist with this one on the same item")
    override val exclusiveWith: List<Ref<RegisteredEnchantment>> = emptyList(),
    @Help("Chance (0-100%) for this enchantment to appear as a book in generated loot chests")
    override val treasureChance: Int = 0,
    @Help("Chance (0-100%) for librarian villagers to sell this enchantment instead of a vanilla book trade")
    override val tradeChance: Int = 0,
    @Help("Whether active triggers use a cooldown")
    val cooldownEnabled: Boolean = true,
    @Help("Cooldown between active triggers")
    val cooldown: Duration = Duration.ofSeconds(1),
    @Help("Criteria that must match for triggers to run")
    val criteria: List<Criteria> = emptyList(),
    @Help("Triggers fired when the enchantment becomes inactive")
    val inactiveTriggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Triggers fired while the enchantment is active. Each entry specifies the level.")
    val activeTriggers: List<ActiveTriggers> = emptyList(),
) : ManifestEntry, RegisteredEnchantment {
    data class Cost(
        val base: Int = 0,
        val perLevel: Int = 0,
    ) {
        fun toEnchantmentCost(): EnchantmentRegistryEntry.EnchantmentCost =
            EnchantmentRegistryEntry.EnchantmentCost.of(base, perLevel)
    }

    data class ActiveTriggers(
        val level: Int = 1,
        val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    )
}


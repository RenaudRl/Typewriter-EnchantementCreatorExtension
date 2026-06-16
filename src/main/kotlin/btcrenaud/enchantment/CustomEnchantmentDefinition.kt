package btcrenaud.enchantment

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.core.extension.annotations.ContentEditor
import com.typewritermc.core.extension.annotations.MaterialProperties
import com.typewritermc.core.extension.annotations.MaterialProperty
import com.typewritermc.engine.paper.entry.ManifestEntry
import com.typewritermc.engine.paper.content.modes.custom.HoldingItemContentMode
import org.bukkit.Material

@Entry(
    name = "custom_enchantment_definition",
    description = "Defines a custom enchantment with modular mechanics",
    color = Colors.BLUE,
    icon = "material-symbols:auto-fix-high"
)
@Tags("custom_enchantment_definition")
class CustomEnchantmentDefinition(
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
    override val minimumCost: EnchantmentDefinition.Cost = EnchantmentDefinition.Cost(),
    @Help("Maximum cost when enchanting")
    override val maximumCost: EnchantmentDefinition.Cost = EnchantmentDefinition.Cost(),
    @Help("List of modular mechanics for this enchantment")
    val mechanics: List<EnchantmentMechanic> = emptyList()
) : ManifestEntry, RegisteredEnchantment

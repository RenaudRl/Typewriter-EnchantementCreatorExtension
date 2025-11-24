package btc.renaud.enchantment

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.ManifestEntry

@Entry(
    name = "vanilla_enchantment_blacklist_definition",
    description = "Blacklist vanilla enchantments from the enchanting table",
    color = Colors.PURPLE,
    icon = "mdi:block-helper",
)
@Tags("vanilla_enchantment_blacklist_definition")
class VanillaEnchantmentBlacklistDefinition(
    override val id: String = "",
    override val name: String = "",
    @Help("Vanilla enchantments that should never appear on the enchanting table")
    val enchantments: List<VanillaEnchantment> = emptyList(),
) : ManifestEntry


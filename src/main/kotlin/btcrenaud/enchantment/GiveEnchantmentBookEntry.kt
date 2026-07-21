package btcrenaud.enchantment

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.logger
import com.typewritermc.engine.paper.utils.Sync
import com.typewritermc.core.utils.launch
import kotlinx.coroutines.Dispatchers
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.ItemFlag
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.asMini
import net.kyori.adventure.text.format.TextDecoration

@Entry(
    name = "give_enchantment_book",
    description = "Give a book with a custom enchantment",
    color = Colors.PURPLE,
    icon = "mdi:book"
)
class GiveEnchantmentBookEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Standard enchantment to apply on the book")
    val enchantment: Ref<EnchantmentDefinition> = emptyRef(),
    @Help("Custom enchantment to apply on the book (takes priority if both are set)")
    val customEnchantment: Ref<CustomEnchantmentDefinition> = emptyRef(),
    @Help("Level of the enchantment")
    val level: Int = 1,
    @Help("Amount of books to give")
    val amount: Int = 1,
    @Help("Display name of the book. {enchantment} and {level} placeholders are supported")
    val bookName: String = "{enchantment} {level}",
    @Help("Lore line showing the enchantment. Supports color codes and {enchantment_lore}/{level} placeholders")
    val enchantmentLore: List<String> = listOf("{enchantment_lore} {level}"),
    @Help("Additional lore for the book. Supports color codes and {enchantment}/{level} placeholders")
    val bookLore: List<String> = emptyList(),
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val def: RegisteredEnchantment = customEnchantment.get() ?: enchantment.get() ?: return
        // All registry and inventory interactions must run on the server
        // thread to avoid touching unbound registry values. Build the book
        // there and hand it to the player in the same hop.
        Dispatchers.Sync.launch {
            // Registration is idempotent: this covers definitions that were
            // published after the extension finished initializing.
            EnchantmentManager.ensureRegistered(def)
            val ench = EnchantmentManager.getEnchantment(def)
            if (ench == null) {
                logger.warning(
                    "Cannot give enchantment book for '${def.name}': the enchantment could not be registered."
                )
                return@launch
            }
            val item = ItemStack(Material.ENCHANTED_BOOK, amount)
            val meta = item.itemMeta as? EnchantmentStorageMeta ?: return@launch
            meta.addStoredEnchant(ench, level, true)
            meta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS)
            val placeholders = mapOf(
                "{enchantment}" to def.displayName,
                "{enchantment_lore}" to def.enchantmentLore.ifBlank { def.displayName },
                "{level}" to toRoman(level)
            )
            val nameText = placeholders.entries.fold(bookName) { acc, (k, v) -> acc.replace(k, v) }
            meta.displayName(nameText.parsePlaceholders(player).asMini())
            val (openTag, closeTag) = def.nameColor.miniColorTags()
            val enchantmentLoreComponents = enchantmentLore.map { line ->
                val processed = placeholders.entries.fold(line) { acc, (k, v) -> acc.replace(k, v) }
                "$openTag$processed$closeTag".parsePlaceholders(player).asMini()
                    .decoration(TextDecoration.ITALIC, false)
            }
            val additionalLoreComponents = bookLore.map { line ->
                val processed = placeholders.entries.fold(line) { acc, (k, v) -> acc.replace(k, v) }
                processed.parsePlaceholders(player).asMini()
            }
            val loreComponents = enchantmentLoreComponents + additionalLoreComponents
            if (loreComponents.isNotEmpty()) {
                meta.lore(loreComponents)
            }
            item.itemMeta = meta
            val leftover = player.inventory.addItem(item)
            if (leftover.isNotEmpty()) {
                leftover.values.forEach { player.world.dropItemNaturally(player.location, it) }
            }
        }
    }
}

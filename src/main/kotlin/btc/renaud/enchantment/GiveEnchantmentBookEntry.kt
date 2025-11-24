package btc.renaud.enchantment

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
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.Sync
import com.typewritermc.core.utils.launch
import kotlinx.coroutines.Dispatchers
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.ItemFlag
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.utils.asMini
import net.kyori.adventure.text.format.NamedTextColor
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
    @Help("Enchantment to apply on the book")
    val enchantment: Ref<EnchantmentDefinition> = emptyRef(),
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
        val def = enchantment.get() ?: return
        // All operations interacting with Bukkit APIs must run on the main
        // thread to avoid accessing unbound registry values. Construct the
        // enchanted book synchronously and then hand it to the player.
        Dispatchers.Sync.launch {
            // Ensure the enchantment exists before creating the book. This
            // mirrors vanilla behaviour and allows books to be generated even
            // if definitions were loaded after the extension was initialized.
            EnchantmentManager.ensureRegistered(def)
            val ench = EnchantmentManager.getEnchantment(def)
            if (ench == null) {
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
        val color = NamedTextColor.NAMES.value(def.nameColor.mini) ?: NamedTextColor.GRAY
        val enchantmentLoreComponents = enchantmentLore.map { line ->
            val processed = placeholders.entries.fold(line) { acc, (k, v) -> acc.replace(k, v) }
            processed.parsePlaceholders(player).asMini()
                .colorIfAbsent(color)
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

    private fun toRoman(number: Int): String {
        var n = number
        val romans = listOf(
            1000 to "M",
            900 to "CM",
            500 to "D",
            400 to "CD",
            100 to "C",
            90 to "XC",
            50 to "L",
            40 to "XL",
            10 to "X",
            9 to "IX",
            5 to "V",
            4 to "IV",
            1 to "I",
        )
        val sb = StringBuilder()
        for ((value, symbol) in romans) {
            while (n >= value) {
                sb.append(symbol)
                n -= value
            }
        }
        return sb.toString()
    }
}


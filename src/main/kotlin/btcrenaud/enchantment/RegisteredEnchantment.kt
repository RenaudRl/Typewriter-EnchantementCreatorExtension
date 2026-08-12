package btcrenaud.enchantment

import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.utils.asMini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlotGroup

/** Equipment slots an enchantment is active in, mirroring [EquipmentSlotGroup]. */
enum class EnchantSlot(val group: EquipmentSlotGroup) {
    ANY(EquipmentSlotGroup.ANY),
    HAND(EquipmentSlotGroup.HAND),
    MAINHAND(EquipmentSlotGroup.MAINHAND),
    OFFHAND(EquipmentSlotGroup.OFFHAND),
    ARMOR(EquipmentSlotGroup.ARMOR),
    HEAD(EquipmentSlotGroup.HEAD),
    CHEST(EquipmentSlotGroup.CHEST),
    LEGS(EquipmentSlotGroup.LEGS),
    FEET(EquipmentSlotGroup.FEET),
    BODY(EquipmentSlotGroup.BODY),
}

@Tags("registered_enchantment")
interface RegisteredEnchantment : Entry {
    val displayName: String
    val nameColor: String
    val supportedItems: List<Material>
    val anvilCost: Int
    val maxLevel: Int
    val weight: Int
    val minimumCost: EnchantmentDefinition.Cost
    val maximumCost: EnchantmentDefinition.Cost
    val enchantmentLore: String
    val activeSlots: List<EnchantSlot>
    val exclusiveWith: List<Ref<RegisteredEnchantment>>
    val treasureChance: Int
    val tradeChance: Int
}

/**
 * Wraps [text] in this MiniMessage color, accepting either a bare tag name
 * ("gray", "#aaaaaa") or an already-formed tag ("<gradient:...>").
 * Non-italic by default so item text renders like vanilla enchantments.
 */
internal fun String.miniColorTags(): Pair<String, String> =
    if (startsWith("<")) this to "" else "<$this>" to "</$this>"

internal fun String.wrapMiniColor(text: String): Component {
    val (open, close) = miniColorTags()
    return "$open$text$close".asMini().decoration(TextDecoration.ITALIC, false)
}

internal fun RegisteredEnchantment.normalizedMaxLevel(): Int =
    EnchantmentRuntime.clampMaxLevel(maxLevel)

internal fun RegisteredEnchantment.normalizedWeight(): Int =
    EnchantmentRuntime.clampWeight(weight)

internal fun RegisteredEnchantment.normalizedActiveSlots(): List<EnchantSlot> =
    activeSlots.ifEmpty { listOf(EnchantSlot.ARMOR, EnchantSlot.HAND) }

/** Formats an enchantment level as roman numerals, like vanilla does. */
internal fun toRoman(number: Int): String {
    var n = number
    val romans = listOf(
        1000 to "M", 900 to "CM", 500 to "D", 400 to "CD",
        100 to "C", 90 to "XC", 50 to "L", 40 to "XL",
        10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I",
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

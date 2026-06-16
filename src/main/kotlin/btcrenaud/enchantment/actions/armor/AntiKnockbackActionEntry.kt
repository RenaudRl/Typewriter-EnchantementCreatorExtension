package btcrenaud.enchantment.actions.armor

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.core.utils.launch
import kotlinx.coroutines.Dispatchers
import com.typewritermc.engine.paper.utils.Sync
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.event.Event

@Entry(
    name = "anti_knockback_action",
    description = "Provides knockback resistance when equipped",
    color = Colors.BROWN,
    icon = "mdi:anvil"
)
class AntiKnockbackActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Amount of knockback resistance to apply (e.g. 0.5 for 50%)")
    val kbResistance: Double = 0.5
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? com.destroystokyo.paper.event.player.PlayerArmorChangeEvent ?: return

        Dispatchers.Sync.launch {
            val attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE) ?: return@launch
            
            // To be safe and clean, we remove old modifiers named by our custom action to avoid endless stacking
            attr.modifiers.filter { it.name == "typewriter_antikb" }.forEach { attr.removeModifier(it) }

            if (!event.newItem.type.isAir && event.oldItem.type != event.newItem.type) {
                attr.addModifier(AttributeModifier(NamespacedKey(com.typewritermc.engine.paper.plugin, "typewriter_antikb"), kbResistance, AttributeModifier.Operation.ADD_NUMBER))
            }
        }
    }
}

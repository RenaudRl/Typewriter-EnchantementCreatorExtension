package btcrenaud.enchantment.actions.armor

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerItemDamageEvent
import kotlin.random.Random

@Entry(
    name = "hardened_armor_action",
    description = "Has a chance to ignore durability loss",
    color = "#8D6E63",
    icon = "mdi:anvil"
)
class HardenedArmorActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to ignore the durability damage (e.g. 0.25 for 25%)")
    val ignoreChance: Double = 0.25
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? PlayerItemDamageEvent ?: return

        // We do not use launch here because we MUST cancel the event synchronously before it finishes
        if (Random.nextDouble() <= ignoreChance) {
            event.isCancelled = true
        }
    }
}

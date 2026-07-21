package btcrenaud.enchantment.actions.tools

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import btcrenaud.enchantment.BukkitEventContextKey
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.utils.Sync
import kotlinx.coroutines.Dispatchers
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.meta.Damageable
import kotlin.random.Random

@Entry(
    name = "indestructible_action",
    description = "Has a chance to repair the tool instead of taking damage when mining",
    color = "#9E9E9E",
    icon = "mdi:anvil"
)
class IndestructibleActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to repair 1 durability instead of taking damage (0.1 = 10%)")
    val repairChance: Double = 0.1
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        
        Dispatchers.Sync.launch {
            if (Random.nextDouble() <= repairChance) {
                val item = player.inventory.itemInMainHand
                val meta = item.itemMeta as? Damageable ?: return@launch
                
                if (meta.damage > 0) {
                    // Repair 2 durability (1 to cancel the break, 1 to actually repair)
                    meta.damage = (meta.damage - 2).coerceAtLeast(0)
                    item.itemMeta = meta
                    player.world.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, player.location.add(0.0, 1.0, 0.0), 3, 0.3, 0.2, 0.3, 0.1)
                }
            }
        }
    }
}

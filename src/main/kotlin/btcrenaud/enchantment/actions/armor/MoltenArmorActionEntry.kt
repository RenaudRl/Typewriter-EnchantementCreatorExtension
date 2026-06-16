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
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "molten_armor_action",
    description = "Sets the attacker on fire when they strike you",
    color = Colors.ORANGE,
    icon = "mdi:fire"
)
class MoltenArmorActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Duration of the fire effect in ticks (20 ticks = 1 second)")
    val fireTicks: Int = 60
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val attacker = event.damager as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            if (attacker.fireTicks < fireTicks) {
                attacker.fireTicks = fireTicks
            }
            player.world.spawnParticle(org.bukkit.Particle.FLAME, attacker.location.add(0.0, 1.0, 0.0), 10, 0.3, 0.3, 0.3, 0.05)
        }
    }
}

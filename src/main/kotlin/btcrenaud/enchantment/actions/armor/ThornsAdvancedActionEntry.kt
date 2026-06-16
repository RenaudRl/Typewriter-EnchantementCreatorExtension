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
    name = "thorns_advanced_action",
    description = "Reflects a percentage of incoming melee damage back to the attacker",
    color = Colors.GREEN,
    icon = "mdi:cactus"
)
class ThornsAdvancedActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Percentage of damage to reflect (e.g. 0.3 for 30%)")
    val reflectPercentage: Double = 0.3
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val attacker = event.damager as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            val reflected = event.damage * reflectPercentage
            if (attacker.health - reflected > 0) {
                attacker.health -= reflected
            } else {
                attacker.health = 0.0
            }
            attacker.world.spawnParticle(org.bukkit.Particle.DAMAGE_INDICATOR, attacker.eyeLocation, 5, 0.4, 0.4, 0.4, 0.1)
        }
    }
}

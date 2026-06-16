package btcrenaud.enchantment.actions.combat

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
import org.bukkit.attribute.Attribute
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "lifesteal_action",
    description = "Heals the player based on the damage dealt",
    color = Colors.RED,
    icon = "mdi:heart-half-full"
)
class LifestealActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Percentage of damage converted to health (e.g. 0.2 for 20%)")
    val healPercentage: Double = 0.2
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return

        Dispatchers.Sync.launch {
            val amount = event.finalDamage * healPercentage
            val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
            
            if (player.health < maxHealth && amount > 0) {
                player.health = (player.health + amount).coerceAtMost(maxHealth)
                player.world.spawnParticle(org.bukkit.Particle.HEART, player.eyeLocation, 3, 0.3, 0.3, 0.3, 0.1)
            }
        }
    }
}

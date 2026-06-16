package btcrenaud.enchantment.actions.bow

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
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "healing_arrow_action",
    description = "Heals the target instead of damaging them",
    color = Colors.PINK,
    icon = "mdi:heart-plus"
)
class HealingArrowActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Amount of health to restore (in half-hearts)")
    val healAmount: Double = 4.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            // Cancel damage and heal instead
            event.damage = 0.0
            event.isCancelled = true

            val maxHealth = target.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
            target.health = (target.health + healAmount).coerceAtMost(maxHealth)
            
            target.world.spawnParticle(org.bukkit.Particle.HEART, target.eyeLocation, 5, 0.5, 0.5, 0.5, 0.1)
        }
    }
}

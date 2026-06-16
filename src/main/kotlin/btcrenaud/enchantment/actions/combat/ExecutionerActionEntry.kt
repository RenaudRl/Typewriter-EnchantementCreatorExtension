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
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "executioner_action",
    description = "Has a chance to instantly kill targets below a certain health threshold",
    color = Colors.RED,
    icon = "mdi:axe"
)
class ExecutionerActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Health threshold percentage to activate execute (e.g. 0.2 means 20% health)")
    val thresholdPercentage: Double = 0.2
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            val maxHealth = target.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
            val currentThreshold = maxHealth * thresholdPercentage

            if (target.health <= currentThreshold) {
                target.health = 0.0 // Instant kill
                target.world.spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, target.eyeLocation, 5, 0.5, 0.5, 0.5, 0.1)
                player.playSound(player.location, org.bukkit.Sound.ENTITY_WITHER_BREAK_BLOCK, 1f, 1f)
            }
        }
    }
}

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
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "armor_pierce_action",
    description = "Ignores armor calculations during melee combat",
    color = Colors.BLUE,
    icon = "mdi:shield-off"
)
class ArmorPierceActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Multiplier of the original damage to convert into true damage (e.g. 0.5 for 50%)")
    val damageBypassPercentage: Double = 0.5
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            val amount = event.damage * damageBypassPercentage
            // Deal true damage by bypassing absorption or armor (we just subtract health safely)
            if (target.health - amount > 0) {
                target.health -= amount
            } else {
                target.health = 0.0
            }
            // Reduce the actual event damage to compensate
            event.damage -= amount
            target.world.spawnParticle(org.bukkit.Particle.ENCHANTED_HIT, target.eyeLocation, 10, 0.3, 0.3, 0.3, 0.1)
        }
    }
}

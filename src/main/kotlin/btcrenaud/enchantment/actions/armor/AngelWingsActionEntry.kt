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
import org.bukkit.event.entity.EntityDamageEvent

@Entry(
    name = "angel_wings_action",
    description = "Reduces or completely negates fall damage",
    color = "#9E9E9E",
    icon = "mdi:feather"
)
class AngelWingsActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Multiplier applied to the fall damage (e.g. 0.0 to negate completely, 0.5 to halve)")
    val damageMultiplier: Double = 0.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageEvent ?: return

        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            if (damageMultiplier <= 0.0) {
                event.isCancelled = true
            } else {
                event.damage *= damageMultiplier
            }
            player.world.spawnParticle(
                org.bukkit.Particle.CLOUD,
                player.location,
                10,
                0.3,
                0.0,
                0.3,
                0.05
            )
        }
    }
}

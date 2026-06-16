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
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.entity.Projectile

@Entry(
    name = "sniper_arrow_action",
    description = "Increases damage based on the distance the arrow traveled",
    color = Colors.GREEN,
    icon = "mdi:crosshairs-gps"
)
class SniperActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Damage multiplier per block traveled")
    val multiplierPerBlock: Double = 0.05,
    @Help("Maximum bonus damage that can be added")
    val maxBonusDamage: Double = 10.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val damager = event.damager as? Projectile ?: return

        val distance = player.location.distance(damager.location)
        val bonus = (distance * multiplierPerBlock).coerceAtMost(maxBonusDamage)

        if (bonus > 0) {
            event.damage += bonus
            player.world.spawnParticle(org.bukkit.Particle.CRIT, damager.location, 10, 0.2, 0.2, 0.2, 0.05)
        }
    }
}

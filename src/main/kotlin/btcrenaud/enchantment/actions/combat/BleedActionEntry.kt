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
import org.bukkit.scheduler.BukkitRunnable
import com.typewritermc.engine.paper.plugin

@Entry(
    name = "bleed_action",
    description = "Applies a bleeding effect over time to the target",
    color = Colors.RED,
    icon = "mdi:water"
)
class BleedActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Damage dealt per tick of bleeding")
    val damagePerTick: Double = 1.0,
    @Help("Number of times the bleed ticks")
    val totalTicks: Int = 3,
    @Help("Delay in server ticks between each bleed tick")
    val intervalTicks: Long = 20L
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            object : BukkitRunnable() {
                var ticks = 0
                override fun run() {
                    if (target.isDead || !target.isValid || ticks >= totalTicks) {
                        cancel()
                        return
                    }

                    // On applique des dégâts purs pour simuler le saignement
                    if (target.health - damagePerTick > 0) {
                        target.health -= damagePerTick
                    } else {
                        target.health = 0.0
                    }
                    com.typewritermc.engine.paper.utils.particles.ParticleRenderer.render(
                        target.world,
                        target.location.add(0.0, 1.0, 0.0),
                        "BLOCK",
                        10,
                        org.bukkit.util.Vector(0.4, 0.4, 0.4),
                        0.0,
                        org.bukkit.Material.REDSTONE_BLOCK.createBlockData()
                    )
                    
                    ticks++
                }
            }.runTaskTimer(plugin as org.bukkit.plugin.Plugin, intervalTicks, intervalTicks)
        }
    }
}

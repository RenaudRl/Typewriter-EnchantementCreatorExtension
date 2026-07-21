package btcrenaud.enchantment.actions.combat

import btcrenaud.enchantment.BukkitEventContextKey
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.plugin
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent

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
    val intervalTicks: Long = 20L,
    @Help("Amount of particles spawned on each bleed tick (0 disables them)")
    val particleCount: Int = 10,
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return
        val interval = intervalTicks.coerceAtLeast(1L)

        // Tick on the target's scheduler so the damage is applied on the
        // entity's owning region thread on Folia (main thread on Paper).
        var elapsed = 0
        target.scheduler.runAtFixedRate(plugin, { task ->
            if (target.isDead || !target.isValid || elapsed >= totalTicks) {
                task.cancel()
                return@runAtFixedRate
            }

            // Raw health reduction so the bleed ignores armor, like a wound.
            target.health = (target.health - damagePerTick).coerceAtLeast(0.0)
            if (particleCount > 0) {
                target.world.spawnParticle(
                    Particle.BLOCK,
                    target.location.add(0.0, 1.0, 0.0),
                    particleCount,
                    0.4,
                    0.4,
                    0.4,
                    0.0,
                    Material.REDSTONE_BLOCK.createBlockData()
                )
            }

            elapsed++
        }, null, interval, interval)
    }
}

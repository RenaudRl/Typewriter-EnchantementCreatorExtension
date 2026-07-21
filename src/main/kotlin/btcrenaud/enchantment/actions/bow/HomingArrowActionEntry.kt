package btcrenaud.enchantment.actions.bow

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
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityShootBowEvent

@Entry(
    name = "homing_arrow_action",
    description = "Modifies a shot arrow to magnetically track the nearest enemy",
    color = Colors.BLUE,
    icon = "mdi:target"
)
class HomingArrowActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Radius in blocks scanned around the arrow for a target")
    val searchRadius: Double = 10.0,
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? EntityShootBowEvent ?: return
        val projectile = event.projectile
        val radius = searchRadius.coerceAtLeast(1.0)

        // Steer on the projectile's scheduler so velocity changes happen on
        // the arrow's owning region thread on Folia (main thread on Paper).
        projectile.scheduler.runAtFixedRate(plugin, { task ->
            if (projectile.isDead || projectile.isOnGround) {
                task.cancel()
                return@runAtFixedRate
            }

            val target = projectile.getNearbyEntities(radius, radius, radius)
                .filterIsInstance<LivingEntity>()
                .filter { it != player && !it.isDead }
                .minByOrNull { it.location.distanceSquared(projectile.location) }

            if (target != null) {
                val direction = target.eyeLocation.toVector()
                    .subtract(projectile.location.toVector())
                    .normalize()
                projectile.velocity = direction.multiply(projectile.velocity.length())
                projectile.world.spawnParticle(Particle.ENCHANTED_HIT, projectile.location, 2, 0.0, 0.0, 0.0, 0.0)
            }
        }, null, 1L, 1L)
    }
}

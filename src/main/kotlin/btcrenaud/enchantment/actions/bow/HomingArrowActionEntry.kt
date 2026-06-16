package btcrenaud.enchantment.actions.bow

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
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
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.scheduler.BukkitRunnable
import com.typewritermc.engine.paper.plugin

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
    override val triggers: List<Ref<TriggerableEntry>> = emptyList()
) : ActionEntry {

    override fun ActionTrigger.execute() {
        // Obtenir l'événement du context (garantit grâce à l'update du listener)
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityShootBowEvent ?: return
        val projectile = event.projectile

        // Optimisation Folia : on track le projectile via un BukkitRunnable lié à l'entité si possible, 
        // ou via coroutine sync. On choisit coroutine.
        Dispatchers.Sync.launch {
            object : BukkitRunnable() {
                override fun run() {
                    if (projectile.isDead || projectile.isOnGround) {
                        cancel()
                        return
                    }

                    // Cherche la cible la plus proche dans un rayon de 10 blocs
                    val nearbyEntities = projectile.getNearbyEntities(10.0, 10.0, 10.0)
                    val target = nearbyEntities.filterIsInstance<LivingEntity>()
                        .filter { it != player && !it.isDead }
                        .minByOrNull { it.location.distanceSquared(projectile.location) }

                    if (target != null) {
                        val direction = target.eyeLocation.toVector().subtract(projectile.location.toVector()).normalize()
                        projectile.velocity = direction.multiply(projectile.velocity.length())
                        // Particules visuelles pour montrer le tracking magnétique
                        projectile.world.spawnParticle(org.bukkit.Particle.ENCHANTED_HIT, projectile.location, 2, 0.0, 0.0, 0.0, 0.0)
                    }
                }
            }.runTaskTimer(plugin as org.bukkit.plugin.Plugin, 1L, 1L)
        }
    }
}

package btcrenaud.enchantment.actions.armor

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
import org.bukkit.event.entity.EntityDamageEvent

@Entry(
    name = "implosion_armor_action",
    description = "Triggers a massive shockwave when your health drops critically low",
    color = Colors.RED,
    icon = "mdi:boombox"
)
class ImplosionArmorActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Health threshold to trigger the implosion (e.g. 4.0 for 2 hearts)")
    val healthThreshold: Double = 4.0,
    @Help("Knockback force applied to enemies within the radius")
    val knockbackForce: Double = 2.0,
    @Help("Radius of the shockwave")
    val radius: Double = 5.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageEvent ?: return

        // We check health minus finalDamage to predict if they will drop below the threshold
        if (player.health - event.finalDamage <= healthThreshold && player.health > healthThreshold) {
            Dispatchers.Sync.launch {
                val enemies = player.getNearbyEntities(radius, radius, radius)
                    .filterIsInstance<LivingEntity>()
                    .filter { it != player && !it.isDead }

                for (enemy in enemies) {
                    val direction = enemy.location.toVector().subtract(player.location.toVector()).normalize()
                    // Push away from player powerfully
                    enemy.velocity = direction.multiply(knockbackForce).setY(knockbackForce * 0.4)
                }

                player.world.createExplosion(player.location, 0.0f, false, false) // Cosmetic explosion
                player.world.spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER, player.location, 1)
            }
        }
    }
}

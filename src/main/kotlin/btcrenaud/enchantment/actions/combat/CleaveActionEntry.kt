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
    name = "cleave_action",
    description = "Deals area-of-effect (AoE) damage to enemies around the target",
    color = Colors.RED,
    icon = "mdi:sword"
)
class CleaveActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Radius of the AoE effect in blocks")
    val radius: Double = 3.0,
    @Help("Multiplier of the original damage applied to surrounding enemies")
    val damageMultiplier: Double = 0.5
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val primaryTarget = event.entity as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            val aoeDamage = event.finalDamage * damageMultiplier
            
            val enemies = primaryTarget.getNearbyEntities(radius, radius, radius)
                .filterIsInstance<LivingEntity>()
                .filter { it != player && !it.isDead && it != primaryTarget }

            for (enemy in enemies) {
                if (enemy.health - aoeDamage > 0) {
                    enemy.health -= aoeDamage
                } else {
                    enemy.health = 0.0
                }
                enemy.world.spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, enemy.eyeLocation, 1, 0.1, 0.1, 0.1, 0.0)
            }
        }
    }
}

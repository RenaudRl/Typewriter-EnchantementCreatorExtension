package btcrenaud.enchantment.actions.combat
import btcrenaud.enchantment.EnchantmentSchedulers

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
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "vampiric_aura_action",
    description = "Heals all nearby allies when an enemy is killed",
    color = Colors.RED,
    icon = "mdi:account-group"
)
class VampiricAuraActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Radius to search for allies to heal")
    val radius: Double = 10.0,
    @Help("Amount of health to restore (in half-hearts)")
    val healAmount: Double = 4.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        EnchantmentSchedulers.runOnPlayer(player) {
            // Check if the attack was lethal
            if (target.health - event.finalDamage <= 0) {
                val allies = target.getNearbyEntities(radius, radius, radius)
                    .filterIsInstance<Player>()
                
                // Heal the attacker as well just in case they aren't in the raw list
                val allToHeal = (allies + player).distinct()

                for (ally in allToHeal) {
                    val maxHealth = ally.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
                    if (ally.health < maxHealth) {
                        ally.health = (ally.health + healAmount).coerceAtMost(maxHealth)
                        ally.world.spawnParticle(org.bukkit.Particle.HEART, ally.eyeLocation, 4, 0.3, 0.3, 0.3, 0.1)
                    }
                }
            }
        }
    }
}

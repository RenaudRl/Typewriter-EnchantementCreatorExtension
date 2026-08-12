package btcrenaud.enchantment.actions.special
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
import btcrenaud.enchantment.BukkitEventContextKey
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.utils.Sync
import kotlinx.coroutines.Dispatchers
import org.bukkit.event.entity.EntityDamageEvent

@Entry(
    name = "elytra_shield_action",
    description = "Reduces kinetic damage taken from flying into walls",
    color = Colors.PURPLE,
    icon = "mdi:shield-airplane"
)
class ElytraShieldActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Percentage of damage to reduce (e.g. 0.5 for 50%)")
    val damageReduction: Double = 0.5
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? EntityDamageEvent ?: return
        
        if (event.cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
            event.damage -= (event.damage * damageReduction)
            EnchantmentSchedulers.runOnPlayer(player) {
                player.world.spawnParticle(org.bukkit.Particle.ENCHANTED_HIT, player.location, 10, 0.5, 0.5, 0.5, 0.1)
                player.playSound(player.location, org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1f, 1.5f)
            }
        }
    }
}

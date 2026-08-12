package btcrenaud.enchantment.actions.armor
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
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import kotlin.random.Random

@Entry(
    name = "deflect_action",
    description = "Chance to deflect incoming projectiles back at the attacker",
    color = Colors.BLUE,
    icon = "mdi:shield-check"
)
class DeflectActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to deflect the projectile (e.g. 0.15 for 15%)")
    val deflectChance: Double = 0.15
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val projectile = event.damager as? Projectile ?: return

        if (Random.nextDouble() <= deflectChance) {
            event.isCancelled = true
            EnchantmentSchedulers.runOnEntity(projectile) {
                projectile.velocity = projectile.velocity.multiply(-1.5)
                projectile.shooter = player // Le joueur devient le tireur officiel
            }
            EnchantmentSchedulers.runOnPlayer(player) {
                player.world.spawnParticle(org.bukkit.Particle.ENCHANTED_HIT, player.location.add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.1)
                player.playSound(player.location, org.bukkit.Sound.ITEM_SHIELD_BLOCK, 1f, 1.5f)
            }
        }
    }
}

package btcrenaud.enchantment.actions.armor
import btcrenaud.enchantment.EnchantmentSchedulers

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
import kotlin.random.Random

@Entry(
    name = "evasion_action",
    description = "Gives a chance to completely dodge and cancel incoming damage",
    color = "#9E9E9E",
    icon = "mdi:run-fast"
)
class EvasionActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to dodge the attack (e.g. 0.05 for 5%)")
    val dodgeChance: Double = 0.05
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return

        if (Random.nextDouble() <= dodgeChance) {
            event.isCancelled = true
            EnchantmentSchedulers.runOnPlayer(player) {
                player.world.spawnParticle(org.bukkit.Particle.CLOUD, player.location.add(0.0, 1.0, 0.0), 20, 0.5, 0.5, 0.5, 0.1)
                player.playSound(player.location, org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.5f)
            }
        }
    }
}

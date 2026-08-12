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
import org.bukkit.attribute.Attribute
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import kotlin.random.Random

@Entry(
    name = "enlightened_armor_action",
    description = "Has a chance to heal you when attacked",
    color = Colors.YELLOW,
    icon = "mdi:star-four-points"
)
class EnlightenedArmorActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to trigger the heal (e.g. 0.1 for 10%)")
    val chance: Double = 0.1,
    @Help("Amount of health to restore (in half-hearts)")
    val healAmount: Double = 2.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return

        EnchantmentSchedulers.runOnPlayer(player) {
            if (Random.nextDouble() <= chance) {
                val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
                if (player.health < maxHealth) {
                    player.health = (player.health + healAmount).coerceAtMost(maxHealth)
                    player.world.spawnParticle(org.bukkit.Particle.HEART, player.eyeLocation, 3, 0.4, 0.4, 0.4, 0.1)
                }
            }
        }
    }
}

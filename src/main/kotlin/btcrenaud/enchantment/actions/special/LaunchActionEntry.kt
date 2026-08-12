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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action

@Entry(
    name = "launch_action",
    description = "Launches the player forward when right clicking with the item",
    color = Colors.BLUE,
    icon = "mdi:rocket-launch"
)
class LaunchActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Multiplier for the launch velocity")
    val power: Double = 2.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? PlayerInteractEvent ?: return
        
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            EnchantmentSchedulers.runOnPlayer(player) {
                val dir = player.location.direction.normalize()
                player.velocity = player.velocity.add(dir.multiply(power))
                player.world.spawnParticle(org.bukkit.Particle.CLOUD, player.location, 20, 0.5, 0.0, 0.5, 0.1)
                player.playSound(player.location, org.bukkit.Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.5f)
            }
        }
    }
}

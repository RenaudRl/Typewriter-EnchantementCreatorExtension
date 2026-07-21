package btcrenaud.enchantment.actions.special

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
    name = "elytra_boost_action",
    description = "Gives a periodic boost while gliding with Elytra when right-clicking",
    color = "#9E9E9E",
    icon = "mdi:bird"
)
class ElytraBoostActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Multiplier for the elytra boost forward")
    val boostMultiplier: Double = 1.5
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? PlayerInteractEvent ?: return
        
        if (player.isGliding && (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK)) {
            Dispatchers.Sync.launch {
                val dir = player.location.direction.normalize()
                player.velocity = player.velocity.add(dir.multiply(boostMultiplier))
                player.world.spawnParticle(org.bukkit.Particle.FIREWORK, player.location, 10, 0.2, 0.2, 0.2, 0.1)
                player.playSound(player.location, org.bukkit.Sound.ENTITY_BAT_TAKEOFF, 1f, 1f)
            }
        }
    }
}

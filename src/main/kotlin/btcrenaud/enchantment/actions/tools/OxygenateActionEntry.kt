package btcrenaud.enchantment.actions.tools

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
import org.bukkit.event.block.BlockBreakEvent

@Entry(
    name = "oxygenate_action",
    description = "Restores breath while breaking blocks underwater",
    color = Colors.BLUE,
    icon = "mdi:diving-scuba"
)
class OxygenateActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Amount of breath ticks to restore per block broken")
    val breathRestored: Int = 30
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        
        Dispatchers.Sync.launch {
            if (player.remainingAir < player.maximumAir) {
                player.remainingAir = (player.remainingAir + breathRestored).coerceAtMost(player.maximumAir)
                player.world.spawnParticle(org.bukkit.Particle.BUBBLE_POP, player.eyeLocation, 5, 0.2, 0.2, 0.2, 0.05)
            }
        }
    }
}

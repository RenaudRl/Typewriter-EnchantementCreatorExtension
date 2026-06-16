package btcrenaud.enchantment.actions.special

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
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.event.player.PlayerMoveEvent

@Entry(
    name = "lava_walker_action",
    description = "Freezes lava under your feet into basalt temporarily",
    color = Colors.ORANGE,
    icon = "mdi:volcano"
)
class LavaWalkerActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Radius of the frozen lava blocks")
    val radius: Int = 2
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? PlayerMoveEvent ?: return
        
        Dispatchers.Sync.launch {
            val loc = player.location
            for (x in -radius..radius) {
                for (z in -radius..radius) {
                    val block = loc.world.getBlockAt(loc.blockX + x, loc.blockY - 1, loc.blockZ + z)
                    if (block.type == Material.LAVA) {
                        block.setType(Material.BASALT, false)
                        
                        // Schedule melt
                        Dispatchers.Default.launch {
                            delay(5000L) // 5 seconds later
                            Dispatchers.Sync.launch {
                                if (block.type == Material.BASALT) {
                                    block.setType(Material.LAVA, false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

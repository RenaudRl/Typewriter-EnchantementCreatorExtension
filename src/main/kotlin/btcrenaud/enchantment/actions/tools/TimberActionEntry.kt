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
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Entry(
    name = "timber_action",
    description = "Chops down an entire tree by breaking its bottom log",
    color = Colors.GREEN,
    icon = "mdi:pine-tree"
)
class TimberActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Maximum amount of logs that can be broken at once")
    val maxLogs: Int = 120
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        val originType = event.block.type
        val item: ItemStack = player.inventory.itemInMainHand
        
        if (!originType.name.contains("LOG") && !originType.name.contains("STEM")) return

        Dispatchers.Sync.launch {
            val toBreak = mutableSetOf<Block>()
            val visited = mutableSetOf<Block>()
            toBreak.add(event.block)

            var brokenCount = 0

            while (toBreak.isNotEmpty() && brokenCount < maxLogs) {
                val current = toBreak.first()
                toBreak.remove(current)
                visited.add(current)

                if (current != event.block) {
                    current.breakNaturally(item, true, true)
                    brokenCount++
                }

                // Check 3x3x3 around the log to catch diagonals of the tree
                for (x in -1..1) {
                    for (y in 0..1) { // Mostly check upwards and same level
                        for (z in -1..1) {
                            if (x == 0 && y == 0 && z == 0) continue
                            val adj = current.getRelative(x, y, z)
                            if ((adj.type.name.contains("LOG") || adj.type.name.contains("STEM")) && !visited.contains(adj) && !toBreak.contains(adj)) {
                                toBreak.add(adj)
                            }
                        }
                    }
                }
            }
        }
    }
}

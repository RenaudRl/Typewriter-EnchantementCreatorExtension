package btcrenaud.enchantment.actions.tools
import btcrenaud.enchantment.EnchantmentSchedulers

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
    name = "vein_miner_action",
    description = "Mines a vein of connected identical ores",
    color = "#8D6E63",
    icon = "mdi:graph"
)
class VeinMinerActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Maximum amount of blocks that can be broken in one vein")
    val maxBlocks: Int = 32
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        val originType = event.block.type
        val item: ItemStack = player.inventory.itemInMainHand
        
        if (!originType.name.contains("ORE")) return

        EnchantmentSchedulers.runOnPlayer(player) {
            val toBreak = mutableSetOf<Block>()
            val visited = mutableSetOf<Block>()
            toBreak.add(event.block)

            var brokenCount = 0

            while (toBreak.isNotEmpty() && brokenCount < maxBlocks) {
                val current = toBreak.first()
                toBreak.remove(current)
                visited.add(current)

                if (current != event.block) {
                    current.breakNaturally(item, true, true)
                    brokenCount++
                }

                // Check 6 adjacent faces
                val faces = listOf(
                    current.getRelative(1, 0, 0), current.getRelative(-1, 0, 0),
                    current.getRelative(0, 1, 0), current.getRelative(0, -1, 0),
                    current.getRelative(0, 0, 1), current.getRelative(0, 0, -1)
                )

                for (adj in faces) {
                    if (adj.type == originType && !visited.contains(adj) && !toBreak.contains(adj)) {
                        toBreak.add(adj)
                    }
                }
            }
        }
    }
}

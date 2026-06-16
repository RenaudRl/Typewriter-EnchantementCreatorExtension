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
import org.bukkit.event.block.BlockBreakEvent

@Entry(
    name = "telepathy_action",
    description = "Places mined blocks and items directly into your inventory",
    color = Colors.PURPLE,
    icon = "mdi:auto-download"
)
class TelepathyActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList()
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        
        if (event.isDropItems) {
            event.isDropItems = false
            val drops = event.block.getDrops(player.inventory.itemInMainHand, player)
            
            for (drop in drops) {
                val leftover = player.inventory.addItem(drop)
                if (leftover.isNotEmpty()) {
                    for ((_, item) in leftover) {
                        player.world.dropItemNaturally(player.location, item)
                    }
                }
            }
            // Add experience directly to player rather than dropping it
            player.giveExp(event.expToDrop)
            event.expToDrop = 0
            
            player.world.spawnParticle(org.bukkit.Particle.PORTAL, event.block.location.add(0.5, 0.5, 0.5), 15, 0.2, 0.2, 0.2, 0.1)
        }
    }
}

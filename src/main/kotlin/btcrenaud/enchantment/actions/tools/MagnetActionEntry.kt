package btcrenaud.enchantment.actions.tools
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
import org.bukkit.entity.Item
import org.bukkit.event.block.BlockBreakEvent

@Entry(
    name = "magnet_action",
    description = "Sucks nearby items towards the player when mining",
    color = Colors.PURPLE,
    icon = "mdi:magnet"
)
class MagnetActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Radius in blocks to suck items from")
    val radius: Double = 6.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        
        EnchantmentSchedulers.runOnPlayerLater(player, 1L) {
            // Briefly delay so the broken block's items have a chance to drop
            val items = player.getNearbyEntities(radius, radius, radius)
                .filterIsInstance<Item>()
                .filter { it.pickupDelay <= 20 }
                
            for (item in items) {
                val direction = player.location.toVector().subtract(item.location.toVector()).normalize()
                item.velocity = direction.multiply(0.8)
                item.pickupDelay = 0
            }
        }
    }
}

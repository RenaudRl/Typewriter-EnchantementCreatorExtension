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
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.utils.Sync
import kotlinx.coroutines.Dispatchers
import org.bukkit.block.data.Ageable
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action

@Entry(
    name = "green_thumb_action",
    description = "Passively bone-meals nearby crops when right-clicking",
    color = Colors.GREEN,
    icon = "mdi:leaf"
)
class GreenThumbActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Radius in blocks to apply the growth effect")
    val radius: Int = 4
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? PlayerInteractEvent ?: return
        
        if (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR) {
            EnchantmentSchedulers.runOnPlayer(player) {
                val loc = player.location
                for (x in -radius..radius) {
                    for (y in -2..2) {
                        for (z in -radius..radius) {
                            val block = loc.world.getBlockAt(loc.blockX + x, loc.blockY + y, loc.blockZ + z)
                            val data = block.blockData
                            if (data is Ageable && data.age < data.maximumAge) {
                                // Simulate bone meal
                                block.applyBoneMeal(org.bukkit.block.BlockFace.UP)
                                player.world.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, block.location.add(0.5, 0.5, 0.5), 2, 0.2, 0.2, 0.2, 0.05)
                            }
                        }
                    }
                }
            }
        }
    }
}

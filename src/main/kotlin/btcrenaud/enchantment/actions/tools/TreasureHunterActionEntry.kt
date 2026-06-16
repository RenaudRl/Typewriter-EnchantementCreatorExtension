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
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

@Entry(
    name = "treasure_hunter_action",
    description = "Mining simple blocks like Dirt or Stone has a chance to drop Emeralds",
    color = Colors.GREEN,
    icon = "mdi:treasure-chest"
)
class TreasureHunterActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to drop a treasure per block broken (e.g. 0.005 for 0.5%)")
    val chance: Double = 0.005
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        val type = event.block.type
        
        if (type == Material.DIRT || type == Material.STONE || type == Material.COBBLESTONE || type == Material.GRASS_BLOCK || type == Material.SAND || type == Material.GRAVEL) {
            Dispatchers.Sync.launch {
                if (Random.nextDouble() <= chance) {
                    player.world.dropItemNaturally(event.block.location, ItemStack(Material.EMERALD))
                    player.playSound(event.block.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f)
                    player.world.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, event.block.location.add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.1)
                }
            }
        }
    }
}

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
import org.bukkit.block.CreatureSpawner
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

@Entry(
    name = "spawners_silktouch_action",
    description = "Allows the player to mine and collect Mob Spawners",
    color = Colors.GRAY,
    icon = "mdi:pig"
)
class SpawnersSilkTouchActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList()
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        val block = event.block
        
        if (block.type == Material.SPAWNER) {
            val spawner = block.state as? CreatureSpawner ?: return
            
            Dispatchers.Sync.launch {
                event.expToDrop = 0
                val spawnerItem = ItemStack(Material.SPAWNER)
                val meta = spawnerItem.itemMeta as BlockStateMeta
                val metaState = meta.blockState as CreatureSpawner
                
                metaState.spawnedType = spawner.spawnedType
                meta.blockState = metaState
                spawnerItem.itemMeta = meta
                
                player.world.dropItemNaturally(block.location, spawnerItem)
                player.world.spawnParticle(org.bukkit.Particle.ENCHANT, block.location.add(0.5, 0.5, 0.5), 30, 0.3, 0.3, 0.3, 0.1)
            }
        }
    }
}

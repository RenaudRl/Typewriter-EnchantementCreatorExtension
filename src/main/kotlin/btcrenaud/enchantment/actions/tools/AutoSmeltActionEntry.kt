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
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.FurnaceRecipe

@Entry(
    name = "auto_smelt_action",
    description = "Automatically smelts mined blocks and yields slightly more experience",
    color = Colors.ORANGE,
    icon = "mdi:fire"
)
class AutoSmeltActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList()
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        
        if (event.isDropItems) {
            val drops = event.block.getDrops(player.inventory.itemInMainHand, player)
            var hasSmelted = false
            
            event.isDropItems = false
            
            for (drop in drops) {
                var resultItem = drop.clone()
                val iterator = Bukkit.recipeIterator()
                
                while (iterator.hasNext()) {
                    val recipe = iterator.next()
                    if (recipe is FurnaceRecipe && recipe.inputChoice.test(drop)) {
                        resultItem = recipe.result.clone()
                        resultItem.amount = drop.amount
                        hasSmelted = true
                        break
                    }
                }
                
                event.block.world.dropItemNaturally(event.block.location, resultItem)
            }
            
            if (hasSmelted) {
                event.expToDrop = (event.expToDrop * 1.5).toInt() + 1
                player.world.spawnParticle(org.bukkit.Particle.FLAME, event.block.location.add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0.05)
            } else {
                event.isDropItems = true // Revert if nothing smelted
            }
        }
    }
}

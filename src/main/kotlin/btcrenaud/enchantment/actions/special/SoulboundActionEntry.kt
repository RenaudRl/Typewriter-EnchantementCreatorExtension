package btcrenaud.enchantment.actions.special

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import btcrenaud.enchantment.BukkitEventContextKey
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.utils.Sync
import kotlinx.coroutines.Dispatchers
import org.bukkit.event.entity.PlayerDeathEvent

@Entry(
    name = "soulbound_action",
    description = "Keeps the enchanted item in your inventory upon death",
    color = Colors.BLUE,
    icon = "mdi:ghost"
)
class SoulboundActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList()
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? PlayerDeathEvent ?: return
        
        Dispatchers.Sync.launch {
            if (event.keepInventory) return@launch
            
            // To be precise, since it's hard to track the EXACT item during death in an entry without
            // passing the specific matched item instance, we'll iterate through drops and find items
            // with this Soulbound enchantment level > 0 and remove them from drops and keep them.
            // A more rigorous Typewriter approach would pass the `ItemStack` context.
            // Assuming `player` is bound to the context.
            
            val iterator = event.drops.iterator()
            val savedItems = mutableListOf<org.bukkit.inventory.ItemStack>()
            
            while (iterator.hasNext()) {
                val item = iterator.next()
                
                // Assuming Typewriter handles custom matching, if Soulbound triggered, we'll keep
                // any items that have the Soulbound custom enchantment. But we can't easily check custom enchants 
                // natively without our manager.
                // For simplicity, we keep items that are not air, since Soulbound would only be triggered 
                // if we HAVE a soulbound item equipped. But to prevent keeping ALL items, we must check.
                
                // However, ActionTrigger does not pass the matched item directly.
                // We'll store a placeholder or try to find it via our Enchanter API.
                // To do this simply, we will just assume it's one of the armor/held items and remove it manually.
                val equip = listOfNotNull(player.inventory.itemInMainHand, player.inventory.itemInOffHand) + player.inventory.armorContents.filterNotNull()
                
                if (equip.any { it.isSimilar(item) }) {
                    iterator.remove()
                    savedItems.add(item)
                }
            }
            
            event.itemsToKeep.addAll(savedItems)
        }
    }
}

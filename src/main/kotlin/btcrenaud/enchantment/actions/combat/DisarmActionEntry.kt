package btcrenaud.enchantment.actions.combat

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.core.utils.launch
import kotlinx.coroutines.Dispatchers
import com.typewritermc.engine.paper.utils.Sync
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "disarm_action",
    description = "Forces the attacked player to drop the item in their main hand",
    color = Colors.ORANGE,
    icon = "mdi:sword-cross"
)
class DisarmActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList()
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? Player ?: return

        Dispatchers.Sync.launch {
            val itemInHand = target.inventory.itemInMainHand
            if (!itemInHand.type.isAir) {
                val droppedItem = target.world.dropItemNaturally(target.location, itemInHand)
                droppedItem.pickupDelay = 40 // ~2 seconds before it can be picked up
                target.inventory.setItemInMainHand(null)
                target.playSound(target.location, org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 1f)
            }
        }
    }
}

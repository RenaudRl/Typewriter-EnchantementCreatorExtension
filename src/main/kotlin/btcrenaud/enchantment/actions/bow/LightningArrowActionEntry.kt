package btcrenaud.enchantment.actions.bow

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
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "lightning_arrow_action",
    description = "Strikes lightning where the arrow hits",
    color = Colors.YELLOW,
    icon = "mdi:flash"
)
class LightningArrowActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList()
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity

        Dispatchers.Sync.launch {
            // Visual lightning, applies no fire or extra vanilla AoE damage, purely cosmetic/targeted
            target.world.strikeLightningEffect(target.location)
            // Add custom bonus damage here if desired
        }
    }
}

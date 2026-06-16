package btcrenaud.enchantment.actions.special

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
import org.bukkit.event.player.PlayerMoveEvent
import kotlin.random.Random

@Entry(
    name = "feeder_action",
    description = "Passively restores food while walking",
    color = Colors.ORANGE,
    icon = "mdi:food-drumstick"
)
class FeederActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to recover 1 food point per move tick (0.01 = 1%)")
    val chance: Double = 0.01
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? PlayerMoveEvent ?: return
        
        Dispatchers.Sync.launch {
            if (player.foodLevel < 20 && Random.nextDouble() <= chance) {
                player.foodLevel = (player.foodLevel + 1).coerceAtMost(20)
                player.saturation = (player.saturation + 0.5f).coerceAtMost(player.foodLevel.toFloat())
            }
        }
    }
}

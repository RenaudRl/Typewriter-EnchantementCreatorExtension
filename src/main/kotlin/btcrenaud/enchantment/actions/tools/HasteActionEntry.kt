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
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Entry(
    name = "haste_action",
    description = "Passively grants Haste while breaking blocks",
    color = Colors.YELLOW,
    icon = "mdi:pickaxe"
)
class HasteActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Duration of the Haste effect in ticks applied upon breaking a block")
    val durationTicks: Int = 100,
    @Help("Level of the Haste effect (0 = level 1)")
    val amplifier: Int = 0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        // Technically an action trigger could be bound to BLOCK_BREAK.
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        
        Dispatchers.Sync.launch {
            player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, durationTicks, amplifier))
        }
    }
}

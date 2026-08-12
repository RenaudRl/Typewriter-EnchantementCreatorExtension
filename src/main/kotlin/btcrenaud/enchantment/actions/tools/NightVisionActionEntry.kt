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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Entry(
    name = "night_vision_action",
    description = "Applies Night Vision when interacting with the tool",
    color = Colors.BLUE,
    icon = "mdi:eye"
)
class NightVisionActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Duration of Night Vision in ticks (minutes * 60 * 20)")
    val durationTicks: Int = 3600 // 3 minutes Default
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? PlayerInteractEvent ?: return
        
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            EnchantmentSchedulers.runOnPlayer(player) {
                player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, durationTicks, 0, false, false, true))
                player.playSound(player.location, org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2.0f)
            }
        }
    }
}

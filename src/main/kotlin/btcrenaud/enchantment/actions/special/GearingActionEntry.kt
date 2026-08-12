package btcrenaud.enchantment.actions.special
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
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Entry(
    name = "gearing_action",
    description = "Grants brief speed when hit",
    color = Colors.YELLOW,
    icon = "mdi:shoe-sneaker"
)
class GearingActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Duration of the speed effect in ticks")
    val durationTicks: Int = 100, // 5 seconds
    @Help("Amplifier for the speed effect (0 = Speed 1)")
    val amplifier: Int = 1
) : ActionEntry {

    override fun ActionTrigger.execute() {
        // Assume context is on defend
        val event = context.get(BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        
        EnchantmentSchedulers.runOnPlayer(player) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, durationTicks, amplifier))
        }
    }
}

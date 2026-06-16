package btcrenaud.enchantment.actions.armor

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.core.utils.launch
import kotlinx.coroutines.Dispatchers
import com.typewritermc.engine.paper.utils.Sync
import org.bukkit.event.Event
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Entry(
    name = "health_boost_action",
    description = "Grants extra maximum health when equipped",
    color = Colors.RED,
    icon = "mdi:heart-multiple"
)
class HealthBoostActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Level of the health boost effect (0 = level 1, +4 Max HP)")
    val amplifier: Int = 0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? com.destroystokyo.paper.event.player.PlayerArmorChangeEvent ?: return

        Dispatchers.Sync.launch {
            // Check if equipping or unequipping
            if (!event.newItem.type.isAir && event.oldItem.type != event.newItem.type) {
                // To avoid stacking permanently and since we do not natively have AttributeModifier removal via engine yet,
                // We use a practically infinite potion effect that is cleared on unequip.
                player.removePotionEffect(PotionEffectType.HEALTH_BOOST)
                player.addPotionEffect(PotionEffect(PotionEffectType.HEALTH_BOOST, Int.MAX_VALUE, amplifier, false, false))
            } else if (event.newItem.type.isAir) {
                player.removePotionEffect(PotionEffectType.HEALTH_BOOST)
            }
        }
    }
}

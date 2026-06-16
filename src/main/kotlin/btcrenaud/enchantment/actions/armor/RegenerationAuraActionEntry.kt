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
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Entry(
    name = "regeneration_aura_action",
    description = "Grants passive regeneration when attacked",
    color = Colors.PINK,
    icon = "mdi:heart-plus"
)
class RegenerationAuraActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Duration of the regeneration effect in ticks (20 ticks = 1 second)")
    val durationTicks: Int = 100,
    @Help("Level of the regeneration effect (0 = level 1)")
    val amplifier: Int = 0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return

        Dispatchers.Sync.launch {
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, durationTicks, amplifier))
        }
    }
}

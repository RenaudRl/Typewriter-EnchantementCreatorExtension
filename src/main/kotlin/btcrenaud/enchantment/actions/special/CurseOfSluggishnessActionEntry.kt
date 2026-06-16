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
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Entry(
    name = "curse_of_sluggishness_action",
    description = "Curse: Applies Slowness and Weakness to the user when attacking",
    color = Colors.RED,
    icon = "mdi:snail"
)
class CurseOfSluggishnessActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Duration of the curse effect per hit in ticks")
    val durationTicks: Int = 100
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        
        Dispatchers.Sync.launch {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 0))
            player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 0))
        }
    }
}

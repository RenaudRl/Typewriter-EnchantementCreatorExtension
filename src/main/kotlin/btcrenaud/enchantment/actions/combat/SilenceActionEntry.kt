package btcrenaud.enchantment.actions.combat

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
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Entry(
    name = "silence_action",
    description = "Silences the target, applying severe mining fatigue and weakness to prevent them from fighting back effectively",
    color = Colors.GRAY,
    icon = "mdi:volume-off"
)
class SilenceActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Duration of the silence effect in ticks (20 ticks = 1 second)")
    val durationTicks: Int = 100
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            target.addPotionEffect(PotionEffect(PotionEffectType.MINING_FATIGUE, durationTicks, 4))
            target.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 1))
            target.world.spawnParticle(org.bukkit.Particle.WITCH, target.eyeLocation, 10, 0.4, 0.4, 0.4, 0.05)
        }
    }
}

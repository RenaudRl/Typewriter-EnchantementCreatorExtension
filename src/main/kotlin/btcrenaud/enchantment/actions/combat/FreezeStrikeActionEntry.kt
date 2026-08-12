package btcrenaud.enchantment.actions.combat
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
import com.typewritermc.core.utils.launch
import kotlinx.coroutines.Dispatchers
import com.typewritermc.engine.paper.utils.Sync
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Entry(
    name = "freeze_strike_action",
    description = "Slows and freezes the target",
    color = Colors.BLUE,
    icon = "mdi:snowflake"
)
class FreezeStrikeActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Duration of the slowness effect in ticks (20 ticks = 1 second)")
    val durationTicks: Int = 60,
    @Help("Amount of freeze ticks added to the entity (changes their visual rendering)")
    val visualFreezeTicks: Int = 100
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        EnchantmentSchedulers.runOnEntity(target) {
            target.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 1))
            // Only add visual freeze ticks if they aren't already frozen heavier
            if (target.freezeTicks < visualFreezeTicks) {
                target.freezeTicks = visualFreezeTicks
            }
            target.world.spawnParticle(org.bukkit.Particle.SNOWFLAKE, target.eyeLocation, 10, 0.4, 0.4, 0.4, 0.05)
        }
    }
}

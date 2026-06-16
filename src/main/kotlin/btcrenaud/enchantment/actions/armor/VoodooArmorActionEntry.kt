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
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

@Entry(
    name = "voodoo_armor_action",
    description = "Has a chance to apply Weakness to the attacker",
    color = Colors.PURPLE,
    icon = "mdi:guy-fawkes-mask"
)
class VoodooArmorActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to apply weakness (e.g. 0.15 for 15%)")
    val chance: Double = 0.15,
    @Help("Duration of the weakness effect in ticks")
    val durationTicks: Int = 100,
    @Help("Level of the weakness effect")
    val amplifier: Int = 0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val attacker = event.damager as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            if (Random.nextDouble() <= chance) {
                attacker.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, durationTicks, amplifier))
                attacker.world.spawnParticle(org.bukkit.Particle.WITCH, attacker.eyeLocation, 5, 0.3, 0.3, 0.3, 0.1)
            }
        }
    }
}

package btcrenaud.enchantment.actions.armor
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
    name = "toxic_aura_action",
    description = "Emits a toxic cloud, poisoning nearby enemies when you take heavy damage",
    color = Colors.GREEN,
    icon = "mdi:chemical-weapon"
)
class ToxicAuraActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Minimum damage taken to trigger the aura")
    val minimumDamageThreshold: Double = 6.0,
    @Help("Radius of the toxic aura in blocks")
    val radius: Double = 4.0,
    @Help("Duration of the poison effect applied in ticks (20 ticks = 1 second)")
    val poisonDurationTicks: Int = 100,
    @Help("Level of the poison effect applied")
    val poisonAmplifier: Int = 1
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return

        if (event.finalDamage >= minimumDamageThreshold) {
            EnchantmentSchedulers.runOnPlayer(player) {
                val enemies = player.getNearbyEntities(radius, radius, radius)
                    .filterIsInstance<LivingEntity>()
                    .filter { it != player && !it.isDead }

                for (enemy in enemies) {
                    enemy.addPotionEffect(PotionEffect(PotionEffectType.POISON, poisonDurationTicks, poisonAmplifier))
                }
                
                // Visual aura
                player.world.spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, player.location.add(0.0, 1.0, 0.0), 30, radius / 2.0, 0.5, radius / 2.0, 0.1)
                player.playSound(player.location, org.bukkit.Sound.ENTITY_CREEPER_HURT, 1f, 0.5f)
            }
        }
    }
}

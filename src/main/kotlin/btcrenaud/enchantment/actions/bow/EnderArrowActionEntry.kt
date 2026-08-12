package btcrenaud.enchantment.actions.bow
import btcrenaud.enchantment.EnchantmentSchedulers

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.core.utils.launch
import kotlinx.coroutines.Dispatchers
import com.typewritermc.engine.paper.utils.Sync
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Enderman
import org.bukkit.entity.Endermite
import org.bukkit.entity.Shulker
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "ender_arrow_action",
    description = "Deals massive bonus true damage to End mobs (Enderman, Shulker, Dragon, Endermite)",
    color = Colors.PURPLE,
    icon = "mdi:eye-outline"
)
class EnderArrowActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    val bonusDamage: Double = 10.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        if (target is Enderman || target is Shulker || target is EnderDragon || target is Endermite) {
            EnchantmentSchedulers.runOnEntity(target) {
                target.world.spawnParticle(org.bukkit.Particle.PORTAL, target.location, 30, 0.5, 0.5, 0.5, 0.1)
                
                if (target.health - bonusDamage > 0) {
                    target.health -= bonusDamage
                } else {
                    target.health = 0.0
                }
            }
        }
    }
}

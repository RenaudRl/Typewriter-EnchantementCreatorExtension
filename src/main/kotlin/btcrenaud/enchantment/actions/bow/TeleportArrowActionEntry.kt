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
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent

@Entry(
    name = "teleport_arrow_action",
    description = "Teleports the shooter to the arrow's impact location",
    color = Colors.PURPLE,
    icon = "mdi:auto-fix"
)
class TeleportArrowActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList()
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? ProjectileHitEvent ?: return
        val projectile = event.entity

        EnchantmentSchedulers.runOnPlayer(player) {
            val loc = projectile.location
            loc.yaw = player.location.yaw
            loc.pitch = player.location.pitch
            player.teleportAsync(loc)
            player.playSound(loc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
            projectile.remove()
        }
    }
}

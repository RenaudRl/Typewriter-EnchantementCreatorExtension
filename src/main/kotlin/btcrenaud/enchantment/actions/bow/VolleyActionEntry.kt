package btcrenaud.enchantment.actions.bow

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
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.util.Vector

@Entry(
    name = "volley_arrow_action",
    description = "Shoots additional arrows in a cone shape",
    color = Colors.BLUE,
    icon = "mdi:arrow-split-vertical"
)
class VolleyActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Number of extra arrows to shoot")
    val extraArrows: Int = 2,
    @Help("Spread angle between arrows")
    val spread: Double = 0.2
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityShootBowEvent ?: return
        val originalProjectile = event.projectile

        Dispatchers.Sync.launch {
            val world = originalProjectile.world
            val loc = originalProjectile.location
            val velocity = originalProjectile.velocity

            for (i in 1..extraArrows) {
                // Alternating left/right spread
                val angle = spread * ((i + 1) / 2) * if (i % 2 == 0) -1.0 else 1.0
                
                val newVelocity = Vector(
                    velocity.x * Math.cos(angle) - velocity.z * Math.sin(angle),
                    velocity.y,
                    velocity.x * Math.sin(angle) + velocity.z * Math.cos(angle)
                )

                val newArrow = world.spawnEntity(loc, originalProjectile.type) as org.bukkit.entity.Projectile
                newArrow.shooter = player
                newArrow.velocity = newVelocity
            }
        }
    }
}

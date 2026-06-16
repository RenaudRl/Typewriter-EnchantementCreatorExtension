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
import org.bukkit.event.entity.ProjectileHitEvent

@Entry(
    name = "explosive_arrow_action",
    description = "Causes a projectile to explode on impact without breaking blocks",
    color = Colors.RED,
    icon = "mdi:explosion"
)
class ExplosiveArrowActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Power of the explosion (default 2.0)")
    val power: Float = 2.0f
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? ProjectileHitEvent ?: return
        val projectile = event.entity

        Dispatchers.Sync.launch {
            // Créer une explosion qui ne casse pas de blocs (false, false pour safeFoliasync)
            projectile.world.createExplosion(projectile.location, power, false, false)
            projectile.remove()
        }
    }
}

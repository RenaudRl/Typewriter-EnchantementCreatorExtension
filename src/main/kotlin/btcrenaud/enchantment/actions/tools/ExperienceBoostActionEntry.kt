package btcrenaud.enchantment.actions.tools

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
import org.bukkit.event.block.BlockBreakEvent

@Entry(
    name = "experience_boost_action",
    description = "Mined blocks drop extra experience",
    color = Colors.GREEN,
    icon = "mdi:star-four-points"
)
class ExperienceBoostActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Multiplier for dropped experience (e.g. 2.0 for double XP)")
    val xpMultiplier: Double = 2.0
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return
        
        if (event.expToDrop > 0) {
            event.expToDrop = (event.expToDrop * xpMultiplier).toInt()
            player.world.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, event.block.location.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.1)
        }
    }
}

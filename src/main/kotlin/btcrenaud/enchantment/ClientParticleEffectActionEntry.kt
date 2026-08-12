package btcrenaud.enchantment

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import org.bukkit.Particle

/** Renders a visual particle only for the triggering player when possible. */
@Entry(
    name = "client_particle_effect",
    description = "Render a Typewriter-aware client particle effect",
    color = Colors.CYAN,
    icon = "mdi:creation"
)
class ClientParticleEffectActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Bukkit particle name")
    val particle: String = "ENCHANT",
    @Help("Number of particles, bounded to 256")
    val count: Int = 8,
    @Help("Particle offset on each axis")
    val offset: Double = 0.25,
    @Help("Particle speed")
    val speed: Double = 0.0,
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val safeCount = count.coerceIn(1, EnchantmentRuntime.MAX_COUNT)
        val safeOffset = offset.coerceIn(0.0, 8.0)
        val safeSpeed = speed.coerceIn(0.0, 16.0)
        EnchantmentSchedulers.runOnPlayer(player) {
            val type = runCatching { Particle.valueOf(particle.uppercase()) }
                .getOrDefault(Particle.ENCHANT)
            player.spawnParticle(
                type,
                player.location,
                safeCount,
                safeOffset,
                safeOffset,
                safeOffset,
                safeSpeed,
            )
        }
    }
}

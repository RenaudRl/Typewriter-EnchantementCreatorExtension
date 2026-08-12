package btcrenaud.enchantment

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerEntriesFor
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import org.bukkit.entity.Player

/**
 * Executes event-phase Typewriter actions before Bukkit returns from the event.
 * Typewriter's normal trigger queue is retained for follow-up interactions.
 */
fun List<Ref<out TriggerableEntry>>.triggerEnchantmentActions(
    player: Player,
    context: InteractionContext,
    eventPhase: Boolean,
) {
    if (!eventPhase) {
        triggerEntriesFor(player, context)
        return
    }

    for (ref in this) {
        val entry = ref.get() ?: continue
        val action = entry as? ActionEntry
        if (action == null) {
            listOf(ref).triggerEntriesFor(player, context)
            continue
        }
        if (!action.criteria.matches(player, context)) continue
        val trigger = ActionTrigger(player, context, action)
        with(action) {
            trigger.execute()
        }
        trigger.applyModifiers()
        action.triggers.triggerEntriesFor(player, trigger.context)
    }
}

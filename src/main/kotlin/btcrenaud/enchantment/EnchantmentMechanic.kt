package btcrenaud.enchantment

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.TriggerableEntry

data class EnchantmentMechanic(
    @Help("The event that triggers this mechanic")
    val event: EnchantmentEvent = EnchantmentEvent.PLAYER_ATTACK,
    @Help("Conditions that must be met to execute the actions")
    val criteria: List<Criteria> = emptyList(),
    @Help("Actions executed when criteria are met")
    val actions: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Client-side only effects (particles, sounds) executed via packets")
    val clientSideEffects: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Level required to run this mechanic. 0 means all levels.")
    val runOnLevel: Int = 0,
    @Help("Chance to trigger this mechanic (0-100%).")
    val chance: Int = 100
)

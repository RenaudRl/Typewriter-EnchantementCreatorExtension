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
    @Help("Visual effects associated with this mechanic. They must be implemented by a client-aware action.")
    val clientSideEffects: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Level threshold used by the selected level mode. 0 means all levels.")
    val runOnLevel: Int = 0,
    @Help("How runOnLevel is compared with the active enchantment level")
    val levelMode: EnchantmentLevelMode = EnchantmentLevelMode.EXACT,
    @Help("Execute actions during the Bukkit event phase so they can modify or cancel that event")
    val eventPhase: Boolean = true,
    @Help("Chance to trigger this mechanic (0-100%).")
    val chance: Int = 100
)

package btcrenaud.enchantment.actions.tools
import btcrenaud.enchantment.EnchantmentSchedulers

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import btcrenaud.enchantment.BukkitEventContextKey
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.utils.Sync
import kotlinx.coroutines.Dispatchers
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import kotlin.random.Random

@Entry(
    name = "bedrock_breaker_action",
    description = "Extremely rare chance to successfully mine Bedrock when left-clicking it",
    color = "#8D6E63",
    icon = "mdi:anvil"
)
class BedrockBreakerActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to break bedrock per interaction (e.g. 0.001 for 0.1%)")
    val chance: Double = 0.001
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? PlayerInteractEvent ?: return // Using interact because you can't normally break bedrock
        val block = event.clickedBlock ?: return
        
        if (event.action == Action.LEFT_CLICK_BLOCK && block.type == Material.BEDROCK) {
            EnchantmentSchedulers.runOnPlayer(player) {
                if (Random.nextDouble() <= chance) {
                    block.type = Material.AIR
                    player.world.dropItemNaturally(block.location, org.bukkit.inventory.ItemStack(Material.BEDROCK))
                    player.world.spawnParticle(org.bukkit.Particle.EXPLOSION_EMITTER, block.location.add(0.5, 0.5, 0.5), 1)
                    player.playSound(block.location, org.bukkit.Sound.BLOCK_ANVIL_DESTROY, 1f, 0.5f)
                } else {
                    player.world.spawnParticle(org.bukkit.Particle.CRIT, block.location.add(0.5, 1.0, 0.5), 3, 0.2, 0.2, 0.2, 0.0)
                }
            }
        }
    }
}

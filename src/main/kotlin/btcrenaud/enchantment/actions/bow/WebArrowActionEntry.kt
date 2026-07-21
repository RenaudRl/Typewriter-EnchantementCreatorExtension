package btcrenaud.enchantment.actions.bow

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
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent

@Entry(
    name = "web_arrow_action",
    description = "Encases the target in a temporary cobweb",
    color = "#9E9E9E",
    icon = "mdi:spider-web"
)
class WebArrowActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Duration of the web in milliseconds")
    val durationMs: Long = 3000L
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            val block = target.location.block
            if (block.type == Material.AIR || block.type == Material.CAVE_AIR || block.type == Material.WATER) {
                val originalType = block.type
                val originalData = block.blockData.clone()
                
                block.setType(Material.COBWEB, false)

                // Schedule removal
                Dispatchers.Default.launch {
                    delay(durationMs)
                    Dispatchers.Sync.launch {
                        if (block.type == Material.COBWEB) {
                            block.setBlockData(originalData, false)
                        }
                    }
                }
            }
        }
    }
}

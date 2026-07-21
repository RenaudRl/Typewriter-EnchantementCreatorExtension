package btcrenaud.enchantment.actions.combat

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
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Zombie
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Creeper
import org.bukkit.entity.WitherSkeleton
import org.bukkit.entity.Piglin
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import kotlin.random.Random

@Entry(
    name = "beheading_action",
    description = "Has a chance to drop the victim's head on kill",
    color = "#8D6E63",
    icon = "mdi:account-switch"
)
class BeheadingActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Chance to drop the head on kill (e.g. 0.05 for 5%)")
    val dropChance: Double = 0.05
) : ActionEntry {

    override fun ActionTrigger.execute() {
        val event = context.get(btcrenaud.enchantment.BukkitEventContextKey) as? EntityDamageByEntityEvent ?: return
        val target = event.entity as? LivingEntity ?: return

        Dispatchers.Sync.launch {
            if (target.health - event.finalDamage <= 0 && Random.nextDouble() <= dropChance) {
                var headItem: ItemStack? = null

                when (target) {
                    is Player -> {
                        headItem = ItemStack(Material.PLAYER_HEAD)
                        val meta = headItem.itemMeta as SkullMeta
                        meta.owningPlayer = target
                        headItem.itemMeta = meta
                    }
                    is Zombie -> headItem = ItemStack(Material.ZOMBIE_HEAD)
                    is Skeleton -> headItem = ItemStack(Material.SKELETON_SKULL)
                    is Creeper -> headItem = ItemStack(Material.CREEPER_HEAD)
                    is WitherSkeleton -> headItem = ItemStack(Material.WITHER_SKELETON_SKULL)
                    is Piglin -> headItem = ItemStack(Material.PIGLIN_HEAD)
                }

                if (headItem != null) {
                    target.world.dropItemNaturally(target.location, headItem)
                    player.playSound(player.location, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                }
            }
        }
    }
}

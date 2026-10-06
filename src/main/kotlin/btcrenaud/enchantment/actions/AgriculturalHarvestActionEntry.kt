package btcrenaud.enchantment.actions

import btcrenaud.enchantment.BukkitEventContextKey
import btcrenaud.enchantment.EnchantmentSchedulers
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Min
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Entry(
    name = "agricultural_harvest_action",
    description = "Optimized action for harvesting crops in an AOE, replanting immediately, and merging drops.",
    color = Colors.GREEN,
    icon = "mdi:sprout"
)
class AgriculturalHarvestActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("Radius on the X and Z axis to harvest (e.g., 1 for a 3x3 area)")
    @Min(0)
    val radius: Int = 1,
    @Help("Should crops be automatically replanted (set age to 0)?")
    val autoReplant: Boolean = true,
    @Help("Particle shown over the harvested area. Particles that need extra data are ignored.")
    val particle: Particle = Particle.COMPOSTER,
    @Help("How many particles are shown once per harvest. 0 disables them.")
    @Min(0)
    val particleCount: Int = 15,
    @Help("Sound key played once per harvest (e.g. block.crop.break). Leave blank for no sound.")
    val sound: String = "block.crop.break",
) : ActionEntry {

    /**
     * Harvests around the block of the [BlockBreakEvent] that fired the enchantment, so it only does
     * something on a `BLOCK_BREAK` mechanic. The broken block itself is left to the normal break.
     */
    override fun ActionTrigger.execute() {
        val event = context.get(BukkitEventContextKey) as? BlockBreakEvent ?: return

        val center = event.block.location
        // Read on the player's own thread; the region thread that edits blocks must not touch the inventory.
        val tool = player.inventory.itemInMainHand.clone()

        EnchantmentSchedulers.runAtLocation(center) {
            harvest(center, tool)
            EnchantmentSchedulers.runOnPlayer(player) { showFeedback(player, center) }
        }
    }

    private fun harvest(center: Location, tool: ItemStack) {
        val world = center.world
        val itemsToDrop = mutableMapOf<Material, Int>()

        for ((dx, dz) in harvestOffsets(radius)) {
            val currentBlock = world.getBlockAt(center.blockX + dx, center.blockY, center.blockZ + dz)
            val blockData = currentBlock.blockData
            if (blockData !is Ageable || blockData.age != blockData.maximumAge) continue

            currentBlock.getDrops(tool).forEach { drop ->
                itemsToDrop.merge(drop.type, drop.amount, Int::plus)
            }

            if (autoReplant) {
                // Direct age reset, without a physics update, keeps a large harvest cheap.
                blockData.age = 0
                currentBlock.setBlockData(blockData, false)
            } else {
                currentBlock.setType(Material.AIR, false)
            }
        }

        // Merged stacks instead of one item entity per drop.
        val dropLocation = center.clone().add(0.5, 0.5, 0.5)
        itemsToDrop.forEach { (material, amount) ->
            stackSizes(amount, material.maxStackSize).forEach { size ->
                world.dropItemNaturally(dropLocation, ItemStack(material, size))
            }
        }
    }

    private fun showFeedback(player: Player, center: Location) {
        if (particleCount > 0 && particle.dataType == Void::class.java) {
            player.spawnParticle(particle, center, particleCount, radius.toDouble(), 0.5, radius.toDouble(), 0.1)
        }
        if (sound.isNotBlank()) {
            player.playSound(center, sound, 1f, 1f)
        }
    }
}

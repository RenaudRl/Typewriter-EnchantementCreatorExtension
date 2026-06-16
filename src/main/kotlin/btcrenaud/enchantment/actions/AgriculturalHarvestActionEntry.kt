package btcrenaud.enchantment.actions

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
import org.bukkit.Material
import org.bukkit.block.data.Ageable
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
    val radius: Int = 1,
    @Help("Should crops be automatically replanted (set age to 0)?")
    val autoReplant: Boolean = true
) : ActionEntry {

    override fun ActionTrigger.execute() {
        // Obtenir la cible locale (le bloc initialement cassé) du context 
        // L'action est conçue pour être déclenchée via EnchantmentEvent.BLOCK_BREAK.
        // Comme le context de l'engine n'expose pas directement le bloc, on prend la ligne de vue.
        val targetBlock = player.getTargetBlockExact(5) ?: return
        
        // Optimisation Folia : on s'assure d'exécuter la modification du monde en Sync sur la région
        Dispatchers.Sync.launch {
            val world = targetBlock.world
            val centerLoc = targetBlock.location
            val itemsToDrop = mutableMapOf<Material, Int>()

            for (x in -radius..radius) {
                for (z in -radius..radius) {
                    val currentBlock = world.getBlockAt(centerLoc.blockX + x, centerLoc.blockY, centerLoc.blockZ + z)
                    val blockData = currentBlock.blockData

                    if (blockData is Ageable && blockData.age == blockData.maximumAge) {
                        // Récolte les items (simulation de cassure)
                        val drops = currentBlock.getDrops(player.inventory.itemInMainHand, player)
                        drops.forEach { drop ->
                            itemsToDrop[drop.type] = (itemsToDrop[drop.type] ?: 0) + drop.amount
                        }

                        if (autoReplant) {
                            // Replantage hyper-optimisé (pas de physique complète)
                            blockData.age = 0
                            currentBlock.setBlockData(blockData, false)
                        } else {
                            currentBlock.setType(Material.AIR, false)
                        }
                    }
                }
            }

            // Drop les items fusionnés afin de réduire les entités "Item"
            itemsToDrop.forEach { (mat, amount) ->
                var remaining = amount
                while (remaining > 0) {
                    val amountToDrop = remaining.coerceAtMost(mat.maxStackSize)
                    world.dropItemNaturally(player.location, ItemStack(mat, amountToDrop))
                    remaining -= amountToDrop
                }
            }

            // Client-side effects : afficher une seule particule ou effet au lieu de 9
            com.typewritermc.engine.paper.utils.particles.ParticleRenderer.render(
                player,
                centerLoc,
                org.bukkit.Particle.COMPOSTER.name,
                15,
                org.bukkit.util.Vector(radius.toDouble(), 0.5, radius.toDouble()),
                0.1f
            )
            player.playSound(centerLoc, org.bukkit.Sound.BLOCK_CROP_BREAK, 1f, 1f)
        }
    }
}

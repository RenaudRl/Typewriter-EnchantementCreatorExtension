package btcrenaud.enchantment

import com.typewritermc.engine.paper.plugin
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * Small platform seam for server-bound work. The public artifact targets Paper;
 * the custom artifact maps the same calls to BTC/Folia region schedulers.
 */
object EnchantmentSchedulers {
    fun runOnPlayer(player: Player, block: () -> Unit) {
        if (!player.isOnline) return
        player.scheduler.run(plugin, { block() }, null)
    }

    fun runOnEntity(entity: Entity, block: () -> Unit) {
        if (!entity.isValid) return
        entity.scheduler.run(plugin, { block() }, null)
    }

    fun runOnPlayerLater(player: Player, delayTicks: Long, block: () -> Unit) {
        if (!player.isOnline) return
        player.scheduler.runDelayed(plugin, { block() }, null, delayTicks.coerceAtLeast(1L))
    }

    fun runOnEntityLater(entity: Entity, delayTicks: Long, block: () -> Unit) {
        if (!entity.isValid) return
        entity.scheduler.runDelayed(plugin, { block() }, null, delayTicks.coerceAtLeast(1L))
    }
}

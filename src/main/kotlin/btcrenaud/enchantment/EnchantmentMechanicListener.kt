package btcrenaud.enchantment

import com.typewritermc.core.interaction.context
import com.typewritermc.core.extension.Initializable
import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerEntriesFor
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.server
import lirand.api.extensions.server.registerEvents
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import kotlin.random.Random

@Singleton
object EnchantmentMechanicListener : Initializable, Listener {

    override suspend fun initialize() {
        plugin.registerEvents(this)
    }

    override suspend fun shutdown() {
        org.bukkit.event.HandlerList.unregisterAll(this)
    }

    private fun processEvent(player: Player, eventType: EnchantmentEvent, bukkitEvent: org.bukkit.event.Event) {
        val customDefs = EnchantmentManager.definitions()
            .asSequence()
            .filterIsInstance<CustomEnchantmentDefinition>()
            .toList()
        if (customDefs.isEmpty()) return

        for (def in customDefs) {
            val match = EnchantmentManager.activeEquipment(player, def)
            val level = match.level
            if (level <= 0) continue

            for (mechanic in def.mechanics) {
                if (mechanic.event != eventType) continue
                if (!EnchantmentRuntime.matchesLevel(mechanic.levelMode, mechanic.runOnLevel, level)) continue
                if (Random.nextInt(100) >= EnchantmentRuntime.clampChance(mechanic.chance)) continue
                val ctx = context {
                    put(BukkitEventContextKey, bukkitEvent)
                    put(EnchantmentLevelContextKey, level)
                    match.slot?.let { put(EnchantmentSlotContextKey, it) }
                    put(EnchantmentLevelModeContextKey, mechanic.levelMode)
                }
                if (!mechanic.criteria.matches(player, ctx)) continue

                mechanic.actions.triggerEnchantmentActions(player, ctx, mechanic.eventPhase)
                mechanic.clientSideEffects.triggerEntriesFor(player, ctx)
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val entity = event.entity

        if (damager is Player) {
            processEvent(damager, EnchantmentEvent.PLAYER_ATTACK, event)
        }
        if (entity is Player) {
            processEvent(entity, EnchantmentEvent.PLAYER_DEFEND, event)
            if (entity.isBlocking) {
                processEvent(entity, EnchantmentEvent.SHIELD_BLOCK, event)
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onToggleSneak(event: org.bukkit.event.player.PlayerToggleSneakEvent) {
        if (!event.isSneaking) return
        processEvent(event.player, EnchantmentEvent.SNEAK, event)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onToggleSprint(event: org.bukkit.event.player.PlayerToggleSprintEvent) {
        if (!event.isSprinting) return
        processEvent(event.player, EnchantmentEvent.SPRINT, event)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        processEvent(event.player, EnchantmentEvent.BLOCK_BREAK, event)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onEntityShootBow(event: EntityShootBowEvent) {
        val shooter = event.entity as? Player ?: return
        processEvent(shooter, EnchantmentEvent.BOW_SHOOT, event)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onProjectileHit(event: ProjectileHitEvent) {
        val shooter = event.entity.shooter as? Player ?: return
        processEvent(shooter, EnchantmentEvent.PROJECTILE_HIT, event)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        processEvent(event.player, EnchantmentEvent.ITEM_CONSUME, event)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        processEvent(event.entity, EnchantmentEvent.PLAYER_DEATH, event)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        processEvent(event.player, EnchantmentEvent.PLAYER_INTERACT, event)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onArmorChange(event: com.destroystokyo.paper.event.player.PlayerArmorChangeEvent) {
        processEvent(event.player, EnchantmentEvent.ARMOR_EQUIP, event)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onEntityDamage(event: org.bukkit.event.entity.EntityDamageEvent) {
        if (event is EntityDamageByEntityEvent) return
        val player = event.entity as? Player ?: return
        if (event.cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) {
            processEvent(player, EnchantmentEvent.FALL_DAMAGE, event)
        }
        processEvent(player, EnchantmentEvent.ENVIRONMENTAL_DAMAGE, event)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onItemDamage(event: org.bukkit.event.player.PlayerItemDamageEvent) {
        processEvent(event.player, EnchantmentEvent.ITEM_DAMAGE, event)
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onPlayerMove(event: org.bukkit.event.player.PlayerMoveEvent) {
        val player = event.player
        // Anti-lag: Only calculate if changing blocks, and throttle 
        if (event.from.blockX == event.to.blockX && event.from.blockY == event.to.blockY && event.from.blockZ == event.to.blockZ) return
        
        if (player.ticksLived % 4 == 0) {
            processEvent(player, EnchantmentEvent.PLAYER_MOVE, event)
        }
    }
}

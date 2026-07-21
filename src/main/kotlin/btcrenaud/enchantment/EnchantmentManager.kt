package btcrenaud.enchantment

import com.typewritermc.core.extension.Initializable
import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.core.entries.Query
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.entry.triggerEntriesFor
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.server
import lirand.api.extensions.events.unregister
import lirand.api.extensions.server.registerEvents
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.event.WritableRegistry
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.set.RegistrySet
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.logging.Level
import kotlin.random.Random

/** Ticks between two activation checks for every online player. */
private const val CHECK_PERIOD_TICKS = 5L

/** How long computed equipment levels stay valid without an invalidating event. */
private const val LEVEL_CACHE_TTL_MS = 2000L

@Singleton
object EnchantmentManager : Initializable, Listener {
    private val enchantments = ConcurrentHashMap<TypedKey<Enchantment>, Pair<RegisteredEnchantment, Enchantment>>()
    private val byDefinition = ConcurrentHashMap<RegisteredEnchantment, MutableSet<Enchantment>>()
    private val lastRun = ConcurrentHashMap<Pair<UUID, RegisteredEnchantment>, Long>()
    internal val active = ConcurrentHashMap<UUID, MutableSet<RegisteredEnchantment>>()
    private val levelCache = ConcurrentHashMap<UUID, CachedLevels>()
    private val dirtyEquipment: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private var vanillaBlacklist: Set<Enchantment> = emptySet()
    private var task: ScheduledTask? = null
    private var registry: Any? = null

    private data class CachedLevels(val levels: Map<RegisteredEnchantment, Int>, val computedAt: Long)

    override suspend fun initialize() {
        if (registry == null) {
            registry = obtainRegistry()
        }
        val definitions = Query.find<EnchantmentDefinition>().toList()
        val customDefinitions = Query.find<CustomEnchantmentDefinition>().toList()
        definitions.forEach { ensureRegistered(it) }
        customDefinitions.forEach { ensureRegistered(it) }
        reloadVanillaBlacklist()
        plugin.registerEvents(this)
        // Global heartbeat that fans out per-player work onto each player's
        // scheduler, so inventory reads stay region-safe on Folia and behave
        // identically on Paper.
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(
            plugin,
            { _ -> tick() },
            CHECK_PERIOD_TICKS,
            CHECK_PERIOD_TICKS,
        )
    }

    override suspend fun shutdown() {
        task?.cancel()
        task = null
        unregister()
        // Keep enchantments registered to prevent deserialization failures
        // in other plugins that may still load/save item stacks during
        // shutdown. They will be cleared when the server stops.
        enchantments.clear()
        byDefinition.clear()
        lastRun.clear()
        active.clear()
        levelCache.clear()
        dirtyEquipment.clear()
        vanillaBlacklist = emptySet()
        registry = null
    }

    private fun buildDescription(def: RegisteredEnchantment): net.kyori.adventure.text.Component {
        val name = def.displayName.ifBlank { def.name }
        return def.nameColor.wrapMiniColor(name)
    }

    private fun reloadVanillaBlacklist() {
        val definitions = Query.find<VanillaEnchantmentBlacklistDefinition>().toList()
        val resolved = mutableSetOf<Enchantment>()
        val missing = mutableSetOf<VanillaEnchantment>()
        for (definition in definitions) {
            for (entry in definition.enchantments) {
                val enchantment = entry.resolve()
                if (enchantment != null) {
                    resolved += enchantment
                } else {
                    missing += entry
                }
            }
        }
        vanillaBlacklist = resolved
        if (missing.isNotEmpty()) {
            plugin.logger.warning(
                "Unable to resolve vanilla enchantments for blacklist: ${missing.joinToString { it.displayName }}"
            )
        }
    }

    private fun obtainRegistry(): Any {
        val access = RegistryAccess.registryAccess()
        return runCatching {
            val method = access.javaClass.getMethod("getWritableRegistry", RegistryKey::class.java)
            method.invoke(access, RegistryKey.ENCHANTMENT)
        }.getOrElse {
            access.getRegistry(RegistryKey.ENCHANTMENT)
        }
    }

    private fun register(
        def: RegisteredEnchantment,
        registry: WritableRegistry<Enchantment, EnchantmentRegistryEntry.Builder>
    ) {
        val access = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        val keyNames = listOf(sanitize(def.name), sanitize(def.id)).distinct()
        for (keyName in keyNames) {
            val key = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$keyName")
            access.get(key)?.let {
                enchantments[key] = def to it
                byDefinition.getOrPut(def) { ConcurrentHashMap.newKeySet() }.add(it)
                continue
            }
            withUnfrozen(registry) {
                registry.register(key) { b ->
                    b.description(buildDescription(def))
                        .supportedItems(
                            RegistrySet.keySetFromValues(
                                RegistryKey.ITEM,
                                def.supportedItems.mapNotNull { it.asItemType() }
                            )
                        )
                        .anvilCost(def.anvilCost)
                        .maxLevel(def.maxLevel)
                        .weight(def.weight)
                        .minimumCost(def.minimumCost.toEnchantmentCost())
                        .maximumCost(def.maximumCost.toEnchantmentCost())
                        .activeSlots(def.slotGroups())
                }
            }

            // Force the main registry to re-freeze so the new entry becomes bound.
            withUnfrozen(access) { }

            val enchantment = access.get(key) ?: continue
            enchantments[key] = def to enchantment
            byDefinition.getOrPut(def) { ConcurrentHashMap.newKeySet() }.add(enchantment)
        }
    }

    private fun registerReflective(def: RegisteredEnchantment, registry: Any) {
        val keyNames = listOf(sanitize(def.name), sanitize(def.id)).distinct()
        for (keyName in keyNames) {
            val key = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$keyName")
            try {
                val registryClass = registry.javaClass
                val get = registryClass.getMethod("get", TypedKey::class.java)
                val existing = get.invoke(registry, key) as? Enchantment
                if (existing != null) {
                    enchantments[key] = def to existing
                    byDefinition.getOrPut(def) { ConcurrentHashMap.newKeySet() }.add(existing)
                    continue
                }

                val conversionsClass = Class.forName("io.papermc.paper.registry.data.util.Conversions")
                val conversions = conversionsClass.getMethod("global").invoke(null)

                val register = registryClass.getMethod(
                    "register",
                    TypedKey::class.java,
                    Consumer::class.java,
                    conversionsClass
                )

                val consumer = Consumer<Any> { factory ->
                    val empty = factory.javaClass.getMethod("empty")
                    val builder = empty.invoke(factory) as EnchantmentRegistryEntry.Builder
                    builder.description(buildDescription(def))
                        .supportedItems(
                            RegistrySet.keySetFromValues(
                                RegistryKey.ITEM,
                                def.supportedItems.mapNotNull { it.asItemType() }
                            )
                        )
                        .anvilCost(def.anvilCost)
                        .maxLevel(def.maxLevel)
                        .weight(def.weight)
                        .minimumCost(def.minimumCost.toEnchantmentCost())
                        .maximumCost(def.maximumCost.toEnchantmentCost())
                        .activeSlots(def.slotGroups())
                }

                withUnfrozen(registry) {
                    register.invoke(registry, key, consumer, conversions)
                }
                val ench = get.invoke(registry, key) as? Enchantment
                if (ench == null) {
                    continue
                }
                enchantments[key] = def to ench
                byDefinition.getOrPut(def) { ConcurrentHashMap.newKeySet() }.add(ench)
            } catch (e: Exception) {
                plugin.logger.log(Level.WARNING, "Failed to reflectively register ${def.name} as $keyName", e)
            }
        }
    }

    private inline fun withUnfrozen(target: Any, block: () -> Unit) {
        val holder = runCatching {
            val field = target.javaClass.getDeclaredField("registry")
            field.isAccessible = true
            field.get(target)
        }.getOrNull() ?: target

        val frozenField = runCatching {
            holder.javaClass.getDeclaredField("frozen").apply { isAccessible = true }
        }.getOrNull()

        if (frozenField == null) {
            block()
            return
        }

        val wasFrozen = frozenField.getBoolean(holder)
        if (wasFrozen) {
            frozenField.setBoolean(holder, false)
        }

        try {
            block()
        } finally {
            if (wasFrozen) {
                // Re-freeze using the registry's freeze() method when available to
                // ensure all newly registered values become bound. Falling back to
                // simply toggling the field mirrors the previous behaviour.
                runCatching {
                    val freeze = holder.javaClass.getMethod("freeze")
                    freeze.isAccessible = true
                    freeze.invoke(holder)
                }.onFailure {
                    frozenField.setBoolean(holder, true)
                }
            }
        }
    }

    fun ensureRegistered(def: RegisteredEnchantment) {
        val mainName = sanitize(def.name)
        val keyMain = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$mainName")
        val access = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        access.get(keyMain)?.let { existing ->
            enchantments[keyMain] = def to existing
            byDefinition.getOrPut(def) { ConcurrentHashMap.newKeySet() }.add(existing)
            val legacyName = sanitize(def.id)
            if (legacyName != mainName) {
                val legacyKey = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$legacyName")
                access.get(legacyKey)?.let { e ->
                    enchantments[legacyKey] = def to e
                    byDefinition.getOrPut(def) { ConcurrentHashMap.newKeySet() }.add(e)
                }
            }
            return
        }

        val reg = registry ?: obtainRegistry().also { registry = it }
        when (reg) {
            is WritableRegistry<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                register(def, reg as WritableRegistry<Enchantment, EnchantmentRegistryEntry.Builder>)
            }
            else -> registerReflective(def, reg)
        }

        val main = access.get(keyMain)
        if (main != null) {
            enchantments[keyMain] = def to main
            byDefinition.getOrPut(def) { ConcurrentHashMap.newKeySet() }.add(main)
        }
        val legacyName = sanitize(def.id)
        if (legacyName != mainName) {
            val legacyKey = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$legacyName")
            access.get(legacyKey)?.let { e ->
                enchantments[legacyKey] = def to e
                byDefinition.getOrPut(def) { ConcurrentHashMap.newKeySet() }.add(e)
            }
        }
    }

    private fun unregister(key: TypedKey<Enchantment>) {
        val reg = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        try {
            val method = reg.javaClass.getMethod("unregister", TypedKey::class.java)
            withUnfrozen(reg) { method.invoke(reg, key) }
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to unregister enchantment key $key", e)
        }
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        val ctx = context()
        val entries = byDefinition.map { it.key to it.value.toList() }
        if (entries.isEmpty()) return
        for (player in server.onlinePlayers) {
            player.scheduler.run(plugin, { _ -> checkPlayer(player, now, ctx, entries) }, null)
        }
    }

    private fun checkPlayer(
        player: Player,
        now: Long,
        ctx: com.typewritermc.core.interaction.InteractionContext,
        entries: List<Pair<RegisteredEnchantment, List<Enchantment>>>
    ) {
        if (!player.isOnline) return
        val playerActive = active.getOrPut(player.uniqueId) { ConcurrentHashMap.newKeySet() }
        val levels = equipmentLevels(player, now, entries)
        for ((def, _) in entries) {
            val level = levels[def] ?: 0
            val wasActive = def in playerActive
            if (def is EnchantmentDefinition) {
                if (def.cooldownEnabled) {
                    if (level > 0 && def.criteria.matches(player, ctx)) {
                        val last = lastRun.getOrPut(player.uniqueId to def) { 0L }
                        if (now - last >= def.cooldown.toMillis()) {
                            def.activeTriggers.firstOrNull { it.level == level }?.triggers?.triggerEntriesFor(player, ctx)
                            lastRun[player.uniqueId to def] = now
                        }
                        playerActive.add(def)
                    } else if (wasActive) {
                        def.inactiveTriggers.triggerEntriesFor(player, ctx)
                        playerActive.remove(def)
                    }
                } else {
                    if (level > 0 && def.criteria.matches(player, ctx)) {
                        if (!wasActive) {
                            def.activeTriggers.firstOrNull { it.level == level }?.triggers?.triggerEntriesFor(player, ctx)
                            playerActive.add(def)
                        }
                    } else if (wasActive) {
                        def.inactiveTriggers.triggerEntriesFor(player, ctx)
                        playerActive.remove(def)
                    }
                }
            } else if (def is CustomEnchantmentDefinition) {
                if (level > 0) {
                    playerActive.add(def)
                } else if (wasActive) {
                    playerActive.remove(def)
                }
            }
        }
    }

    /**
     * Returns the equipment enchant levels for [player], recomputing them only
     * when an equipment event marked the player dirty or the cache expired.
     */
    private fun equipmentLevels(
        player: Player,
        now: Long,
        entries: List<Pair<RegisteredEnchantment, List<Enchantment>>>,
    ): Map<RegisteredEnchantment, Int> {
        val uuid = player.uniqueId
        val cached = levelCache[uuid]
        val dirty = dirtyEquipment.remove(uuid)
        if (cached != null && !dirty && now - cached.computedAt < LEVEL_CACHE_TTL_MS) {
            return cached.levels
        }
        val equipment = buildList {
            add(player.inventory.itemInMainHand)
            add(player.inventory.itemInOffHand)
            addAll(player.inventory.armorContents.filterNotNull())
        }
        val levels = entries.associate { (def, enchants) ->
            def to (equipment
                .filter { it.type in def.supportedItems }
                .maxOfOrNull { item -> enchants.maxOfOrNull { item.getEnchantmentLevel(it) } ?: 0 }
                ?: 0)
        }
        levelCache[uuid] = CachedLevels(levels, now)
        return levels
    }

    private fun markDirty(uuid: UUID) {
        dirtyEquipment.add(uuid)
    }

    @EventHandler
    private fun onArmorChange(event: com.destroystokyo.paper.event.player.PlayerArmorChangeEvent) = markDirty(event.player.uniqueId)

    @EventHandler
    private fun onHeldItemChange(event: org.bukkit.event.player.PlayerItemHeldEvent) = markDirty(event.player.uniqueId)

    @EventHandler
    private fun onSwapHands(event: org.bukkit.event.player.PlayerSwapHandItemsEvent) = markDirty(event.player.uniqueId)

    @EventHandler
    private fun onInventoryClose(event: org.bukkit.event.inventory.InventoryCloseEvent) {
        (event.player as? Player)?.let { markDirty(it.uniqueId) }
    }

    @EventHandler
    private fun onItemPickup(event: org.bukkit.event.entity.EntityPickupItemEvent) {
        (event.entity as? Player)?.let { markDirty(it.uniqueId) }
    }

    @EventHandler
    private fun onItemDrop(event: org.bukkit.event.player.PlayerDropItemEvent) = markDirty(event.player.uniqueId)

    private fun sanitize(input: String): String =
        input.lowercase(Locale.ROOT)
            .replace("\\s+".toRegex(), "_")
            .replace("[^a-z0-9_./-]".toRegex(), "_")

    private fun RegisteredEnchantment.slotGroups(): List<EquipmentSlotGroup> =
        activeSlots.ifEmpty { listOf(EnchantSlot.ARMOR, EnchantSlot.HAND) }.map { it.group }

    fun getEnchantment(def: RegisteredEnchantment): Enchantment? =
        byDefinition[def]?.firstOrNull()

    /** All registered definitions, for world integration and commands. */
    fun definitions(): Set<RegisteredEnchantment> = byDefinition.keys

    /** Definitions present on [item], counting both direct and stored enchants. */
    fun definitionsOn(item: org.bukkit.inventory.ItemStack): List<RegisteredEnchantment> {
        val stored = item.itemMeta as? org.bukkit.inventory.meta.EnchantmentStorageMeta
        return byDefinition.mapNotNull { (def, enchants) ->
            val has = enchants.any { item.getEnchantmentLevel(it) > 0 || stored?.hasStoredEnchant(it) == true }
            if (has) def else null
        }
    }

    /**
     * Builds an enchanted book for [def] at [level], without player context.
     * Used by loot injection, villager trades and the admin give command.
     */
    fun buildBook(def: RegisteredEnchantment, level: Int): org.bukkit.inventory.ItemStack? {
        ensureRegistered(def)
        val ench = getEnchantment(def) ?: return null
        val lvl = level.coerceIn(1, def.maxLevel.coerceAtLeast(1))
        val item = org.bukkit.inventory.ItemStack(org.bukkit.Material.ENCHANTED_BOOK)
        val meta = item.itemMeta as? org.bukkit.inventory.meta.EnchantmentStorageMeta ?: return null
        meta.addStoredEnchant(ench, lvl, true)
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_STORED_ENCHANTS)
        val displayName = def.displayName.ifBlank { def.name }
        meta.displayName(def.nameColor.wrapMiniColor("$displayName ${toRoman(lvl)}"))
        val lore = def.enchantmentLore.ifBlank { displayName }
        meta.lore(listOf(def.nameColor.wrapMiniColor("$lore ${toRoman(lvl)}")))
        item.itemMeta = meta
        return item
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        active.remove(uuid)
        levelCache.remove(uuid)
        dirtyEquipment.remove(uuid)
        lastRun.keys.removeIf { it.first == uuid }
    }

    @EventHandler
    private fun onAsyncPreLogin(@Suppress("UNUSED_PARAMETER") event: AsyncPlayerPreLoginEvent) {
        val definitions = Query.find<EnchantmentDefinition>().toList()
        val customDefinitions = Query.find<CustomEnchantmentDefinition>().toList()
        // Registration must happen on the global region thread before any other
        // plugin attempts to deserialize the joining player's inventory, so
        // unknown enchantments never corrupt player data. Registration is
        // idempotent: already-known definitions are simply re-linked.
        runCatching {
            Bukkit.getGlobalRegionScheduler().run(plugin) { _ ->
                definitions.forEach { ensureRegistered(it) }
                customDefinitions.forEach { ensureRegistered(it) }
                reloadVanillaBlacklist()
            }
        }.onFailure {
            plugin.logger.log(Level.SEVERE, "Failed to register enchantments during pre-login", it)
        }
    }

    @EventHandler
    private fun onPrepareEnchant(event: PrepareItemEnchantEvent) {
        val random = Random.Default
        val offers = event.offers
        if (vanillaBlacklist.isNotEmpty()) {
            for (i in offers.indices) {
                val offer = offers[i] ?: continue
                if (offer.enchantment in vanillaBlacklist) {
                    offers[i] = null
                }
            }
        }
        byDefinition.forEach { (def, enchants) ->
            val ench = enchants.firstOrNull() ?: return@forEach
            if (def.weight <= 0) return@forEach
            if (event.item.type !in def.supportedItems) return@forEach
            if (random.nextInt(100) >= def.weight) return@forEach
            val slot = random.nextInt(offers.size)
            val level = random.nextInt(def.maxLevel.coerceAtLeast(1)) + 1
            val cost = offers[slot]?.cost ?: (slot + 1)
            offers[slot] = EnchantmentOffer(ench, level, cost)
        }
    }

    @EventHandler
    private fun onPrepareAnvil(event: org.bukkit.event.inventory.PrepareAnvilEvent) {
        val first = event.inventory.getItem(0) ?: return
        val second = event.inventory.getItem(1) ?: return
        val result = event.result ?: return
        for ((def, enchants) in byDefinition) {
            val ench = enchants.firstOrNull() ?: continue
            val hasEnchant = second.itemMeta is org.bukkit.inventory.meta.EnchantmentStorageMeta &&
                (second.itemMeta as org.bukkit.inventory.meta.EnchantmentStorageMeta).hasStoredEnchant(ench)
            if (hasEnchant && first.type !in def.supportedItems) {
                event.result = null
                return
            }
            if (first.getEnchantmentLevel(ench) > 0 && second.type !in def.supportedItems && result.getEnchantmentLevel(ench) > 0) {
                event.result = null
                return
            }
        }

        // Enforce configured incompatibilities: the combined result may never
        // carry two enchantments declared exclusive with each other.
        val resultDefs = definitionsOn(result)
        if (resultDefs.size > 1) {
            for (def in resultDefs) {
                val exclusiveIds = def.exclusiveWith.mapNotNull { it.get()?.id }.toSet()
                if (exclusiveIds.isEmpty()) continue
                if (resultDefs.any { other -> other !== def && other.id in exclusiveIds }) {
                    event.result = null
                    return
                }
            }
        }
    }
}

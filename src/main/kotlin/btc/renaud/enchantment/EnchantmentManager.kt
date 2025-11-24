package btc.renaud.enchantment

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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import com.typewritermc.engine.paper.utils.asMini
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.Locale
import java.util.function.Consumer
import java.util.logging.Level
import kotlin.random.Random

@Singleton
object EnchantmentManager : Initializable, Listener {
    private val enchantments = mutableMapOf<TypedKey<Enchantment>, Pair<EnchantmentDefinition, Enchantment>>()
    private val byDefinition = mutableMapOf<EnchantmentDefinition, MutableSet<Enchantment>>()
    private val lastRun = mutableMapOf<Pair<UUID, EnchantmentDefinition>, Long>()
    private val active = mutableMapOf<UUID, MutableSet<EnchantmentDefinition>>()
    private var vanillaBlacklist: Set<Enchantment> = emptySet()
    private var task: BukkitTask? = null
    private var registry: Any? = null

    private fun buildDescription(def: EnchantmentDefinition): Component {
        val name = def.displayName.ifBlank { def.name }
        val color = NamedTextColor.NAMES.value(def.nameColor.mini) ?: NamedTextColor.GRAY
        return name.asMini().color(color).decoration(TextDecoration.ITALIC, false)
    }

    override suspend fun initialize() {
        if (registry == null) {
            registry = obtainRegistry()
        }
        val definitions = Query.find<EnchantmentDefinition>().toList()
        definitions.forEach { ensureRegistered(it) }
        reloadVanillaBlacklist()
        plugin.registerEvents(this)
        task = server.scheduler.runTaskTimer(plugin, Runnable { tick() }, 1L, 1L)
    }

    override suspend fun shutdown() {
        task?.cancel()
        unregister()
        // Keep enchantments registered to prevent deserialization failures
        // in other plugins that may still load/save item stacks during
        // shutdown. They will be cleared when the server stops.
        enchantments.clear()
        byDefinition.clear()
        lastRun.clear()
        active.clear()
        vanillaBlacklist = emptySet()
        registry = null
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
        def: EnchantmentDefinition,
        registry: WritableRegistry<Enchantment, EnchantmentRegistryEntry.Builder>
    ) {
        val access = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        val keyNames = listOf(sanitize(def.name), sanitize(def.id)).distinct()
        for (keyName in keyNames) {
            val key = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$keyName")
            access.get(key)?.let {
                enchantments[key] = def to it
                byDefinition.getOrPut(def) { mutableSetOf() }.add(it)
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
                        .activeSlots(listOf(EquipmentSlotGroup.ARMOR, EquipmentSlotGroup.HAND))
                }
            }

            // Force the main registry to re-freeze so the new entry becomes bound.
            withUnfrozen(access) { }

            val enchantment = access.get(key) ?: continue
            enchantments[key] = def to enchantment
            byDefinition.getOrPut(def) { mutableSetOf() }.add(enchantment)
        }
    }

    private fun registerReflective(def: EnchantmentDefinition, registry: Any) {
        val keyNames = listOf(sanitize(def.name), sanitize(def.id)).distinct()
        for (keyName in keyNames) {
            val key = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$keyName")
            runCatching {
                val registryClass = registry.javaClass
                val get = registryClass.getMethod("get", TypedKey::class.java)
                val existing = get.invoke(registry, key) as? Enchantment
                if (existing != null) {
                    enchantments[key] = def to existing
                    byDefinition.getOrPut(def) { mutableSetOf() }.add(existing)
                    return@runCatching
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
                        .activeSlots(listOf(EquipmentSlotGroup.ARMOR, EquipmentSlotGroup.HAND))
                }

                withUnfrozen(registry) {
                    register.invoke(registry, key, consumer, conversions)
                }
                val ench = get.invoke(registry, key) as? Enchantment
                if (ench == null) {
                    return@runCatching
                }
                enchantments[key] = def to ench
                byDefinition.getOrPut(def) { mutableSetOf() }.add(ench)
            }.onFailure {
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

    fun ensureRegistered(def: EnchantmentDefinition) {
        val mainName = sanitize(def.name)
        val keyMain = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$mainName")
        val access = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        access.get(keyMain)?.let { existing ->
            enchantments[keyMain] = def to existing
            byDefinition.getOrPut(def) { mutableSetOf() }.add(existing)
            val legacyName = sanitize(def.id)
            if (legacyName != mainName) {
                val legacyKey = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$legacyName")
                access.get(legacyKey)?.let { e ->
                    enchantments[legacyKey] = def to e
                    byDefinition.getOrPut(def) { mutableSetOf() }.add(e)
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
            byDefinition.getOrPut(def) { mutableSetOf() }.add(main)
        }
        val legacyName = sanitize(def.id)
        if (legacyName != mainName) {
            val legacyKey = TypedKey.create(RegistryKey.ENCHANTMENT, "typewriter:$legacyName")
            access.get(legacyKey)?.let { e ->
                enchantments[legacyKey] = def to e
                byDefinition.getOrPut(def) { mutableSetOf() }.add(e)
            }
        }
    }

    private fun unregister(key: TypedKey<Enchantment>) {
        val reg = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        runCatching {
            val method = reg.javaClass.getMethod("unregister", TypedKey::class.java)
            withUnfrozen(reg) { method.invoke(reg, key) }
        }.onFailure {
        }
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        val ctx = context()
        val entries = byDefinition.map { it.key to it.value.toList() }
        for (player in server.onlinePlayers) {
            checkPlayer(player, now, ctx, entries)
        }
    }

    private fun checkPlayer(
        player: Player,
        now: Long,
        ctx: com.typewritermc.core.interaction.InteractionContext,
        entries: List<Pair<EnchantmentDefinition, List<Enchantment>>>
    ) {
        val playerActive = active.getOrPut(player.uniqueId) { mutableSetOf() }
        val equipment = buildList {
            add(player.inventory.itemInMainHand)
            add(player.inventory.itemInOffHand)
            addAll(player.inventory.armorContents)
        }
        for ((def, enchants) in entries) {
            val level = equipment.filterNotNull()
                .filter { it.type in def.supportedItems }
                .maxOfOrNull { item ->
                    enchants.maxOfOrNull { item.getEnchantmentLevel(it) } ?: 0
                } ?: 0
            val wasActive = def in playerActive
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
        }
    }

    private fun sanitize(input: String): String =
        input.lowercase(Locale.ROOT)
            .replace("\\s+".toRegex(), "_")
            .replace("[^a-z0-9_./-]".toRegex(), "_")

    fun getEnchantment(def: EnchantmentDefinition): Enchantment? =
        byDefinition[def]?.firstOrNull()

    @EventHandler
    private fun onAsyncPreLogin(@Suppress("UNUSED_PARAMETER") event: AsyncPlayerPreLoginEvent) {
        val definitions = Query.find<EnchantmentDefinition>().toList()
        // Registration of enchantments must happen on the main thread before
        // any other plugin attempts to deserialize player inventories. Using
        // callSyncMethod blocks the login thread until all definitions are
        // registered, preventing race conditions where unknown enchantments
        // corrupt player data.
        val future = server.scheduler.callSyncMethod(plugin) {
            definitions.forEach { ensureRegistered(it) }
            reloadVanillaBlacklist()
        }
        runCatching { future.get() }.onFailure {
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
    }
}


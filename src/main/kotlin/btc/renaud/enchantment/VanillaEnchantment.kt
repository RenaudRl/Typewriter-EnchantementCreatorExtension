package btc.renaud.enchantment

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import java.util.logging.Level
import java.util.logging.Logger
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

private val logger = Logger.getLogger("btc.renaud.enchantment.VanillaEnchantment")

internal var vanillaEnchantmentResolver: (NamespacedKey) -> Enchantment? = resolver@{ key ->
    val registry = runCatching { RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT) }
        .getOrElse {
            logger.log(
                Level.WARNING,
                "Unable to resolve vanilla enchantment {0} from registry or legacy API.",
                key.asString(),
            )
            return@resolver null
        }

    return@resolver try {
        registry.get(key)
    } catch (_: NoSuchElementException) {
        logger.log(
            Level.WARNING,
            "Unable to resolve vanilla enchantment {0} from registry or legacy API.",
            key.asString(),
        )
        null
    } catch (_: IllegalArgumentException) {
        logger.log(
            Level.WARNING,
            "Unable to resolve vanilla enchantment {0} from registry or legacy API.",
            key.asString(),
        )
        null
    }
}

/**
 * Enumerates vanilla Minecraft enchantments that can be referenced within TypeWriter entries.
 */
enum class VanillaEnchantment(
    private val namespacedKey: NamespacedKey,
    val displayName: String,
) {
    AQUA_AFFINITY(NamespacedKey.minecraft("aqua_affinity"), "Aqua Affinity"),
    BANE_OF_ARTHROPODS(NamespacedKey.minecraft("bane_of_arthropods"), "Bane of Arthropods"),
    BINDING_CURSE(NamespacedKey.minecraft("binding_curse"), "Curse of Binding"),
    BLAST_PROTECTION(NamespacedKey.minecraft("blast_protection"), "Blast Protection"),
    CHANNELING(NamespacedKey.minecraft("channeling"), "Channeling"),
    DEPTH_STRIDER(NamespacedKey.minecraft("depth_strider"), "Depth Strider"),
    DENSITY(NamespacedKey.minecraft("density"), "Density"),
    EFFICIENCY(NamespacedKey.minecraft("efficiency"), "Efficiency"),
    FEATHER_FALLING(NamespacedKey.minecraft("feather_falling"), "Feather Falling"),
    FIRE_ASPECT(NamespacedKey.minecraft("fire_aspect"), "Fire Aspect"),
    FIRE_PROTECTION(NamespacedKey.minecraft("fire_protection"), "Fire Protection"),
    FLAME(NamespacedKey.minecraft("flame"), "Flame"),
    FORTUNE(NamespacedKey.minecraft("fortune"), "Fortune"),
    FROST_WALKER(NamespacedKey.minecraft("frost_walker"), "Frost Walker"),
    IMPALING(NamespacedKey.minecraft("impaling"), "Impaling"),
    INFINITY(NamespacedKey.minecraft("infinity"), "Infinity"),
    KNOCKBACK(NamespacedKey.minecraft("knockback"), "Knockback"),
    LOOTING(NamespacedKey.minecraft("looting"), "Looting"),
    LOYALTY(NamespacedKey.minecraft("loyalty"), "Loyalty"),
    LUCK_OF_THE_SEA(NamespacedKey.minecraft("luck_of_the_sea"), "Luck of the Sea"),
    LURE(NamespacedKey.minecraft("lure"), "Lure"),
    MENDING(NamespacedKey.minecraft("mending"), "Mending"),
    MULTISHOT(NamespacedKey.minecraft("multishot"), "Multishot"),
    PIERCING(NamespacedKey.minecraft("piercing"), "Piercing"),
    POWER(NamespacedKey.minecraft("power"), "Power"),
    PROJECTILE_PROTECTION(NamespacedKey.minecraft("projectile_protection"), "Projectile Protection"),
    PROTECTION(NamespacedKey.minecraft("protection"), "Protection"),
    PUNCH(NamespacedKey.minecraft("punch"), "Punch"),
    QUICK_CHARGE(NamespacedKey.minecraft("quick_charge"), "Quick Charge"),
    RESPIRATION(NamespacedKey.minecraft("respiration"), "Respiration"),
    RIPTIDE(NamespacedKey.minecraft("riptide"), "Riptide"),
    SHARPNESS(NamespacedKey.minecraft("sharpness"), "Sharpness"),
    SILK_TOUCH(NamespacedKey.minecraft("silk_touch"), "Silk Touch"),
    SMITE(NamespacedKey.minecraft("smite"), "Smite"),
    SOUL_SPEED(NamespacedKey.minecraft("soul_speed"), "Soul Speed"),
    SWEEPING_EDGE(NamespacedKey.minecraft("sweeping"), "Sweeping Edge"),
    SWIFT_SNEAK(NamespacedKey.minecraft("swift_sneak"), "Swift Sneak"),
    THORNS(NamespacedKey.minecraft("thorns"), "Thorns"),
    UNBREAKING(NamespacedKey.minecraft("unbreaking"), "Unbreaking"),
    VANISHING_CURSE(NamespacedKey.minecraft("vanishing_curse"), "Curse of Vanishing"),
    WIND_BURST(NamespacedKey.minecraft("wind_burst"), "Wind Burst"),
    BREACH(NamespacedKey.minecraft("breach"), "Breach"),
    WEAVING(NamespacedKey.minecraft("weaving"), "Weaving"),
    ;

    fun resolve(): Enchantment? = vanillaEnchantmentResolver(namespacedKey)

    override fun toString(): String = displayName
}


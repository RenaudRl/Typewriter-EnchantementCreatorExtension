package btcrenaud.enchantment

/**
 * Shared, platform-neutral rules used by the enchantment runtime.
 *
 * Keeping these rules outside listeners makes the public Paper artifact and the
 * BTC custom artifact agree on the same Typewriter data model.
 */
internal object EnchantmentRuntime {
    const val MAX_LEVEL = 255
    const val MAX_WEIGHT = 10_000
    const val MAX_RADIUS = 32.0
    const val MAX_COUNT = 256

    fun clampLevel(level: Int, maxLevel: Int): Int =
        level.coerceIn(1, maxLevel.coerceIn(1, MAX_LEVEL))

    fun clampMaxLevel(maxLevel: Int): Int = maxLevel.coerceIn(1, MAX_LEVEL)

    fun clampChance(chance: Int): Int = chance.coerceIn(0, 100)

    fun clampWeight(weight: Int): Int = weight.coerceIn(0, MAX_WEIGHT)

    fun registryKeyNames(definition: RegisteredEnchantment): List<String> {
        val canonical = sanitize(definition.id).ifBlank { sanitize(definition.name) }
        val legacy = sanitize(definition.name)
        return listOf(canonical, legacy)
            .filter(String::isNotBlank)
            .distinct()
    }

    fun sanitize(value: String): String = value
        .trim()
        .lowercase()
        .replace("\\s+".toRegex(), "_")
        .replace("[^a-z0-9_./-]".toRegex(), "_")
        .trim('_')

    fun matchesLevel(mode: EnchantmentLevelMode, requiredLevel: Int, activeLevel: Int): Boolean {
        if (requiredLevel <= 0) return true
        return when (mode) {
            EnchantmentLevelMode.EXACT -> activeLevel == requiredLevel
            EnchantmentLevelMode.AT_LEAST -> activeLevel >= requiredLevel
            EnchantmentLevelMode.AT_MOST -> activeLevel <= requiredLevel
        }
    }

    fun slotMatches(configured: EnchantSlot, actual: EnchantSlot): Boolean = when (configured) {
        EnchantSlot.ANY -> true
        EnchantSlot.HAND -> actual == EnchantSlot.MAINHAND || actual == EnchantSlot.OFFHAND
        EnchantSlot.ARMOR -> actual == EnchantSlot.HEAD ||
            actual == EnchantSlot.CHEST ||
            actual == EnchantSlot.LEGS ||
            actual == EnchantSlot.FEET ||
            actual == EnchantSlot.BODY
        else -> configured == actual
    }
}

enum class EnchantmentLevelMode {
    EXACT,
    AT_LEAST,
    AT_MOST,
}

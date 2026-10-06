package btcrenaud.enchantment.actions

/**
 * Horizontal offsets (dx, dz) of the square of blocks harvested around the broken one, the center
 * included. A negative [radius] is treated as 0, so the area is never empty.
 */
internal fun harvestOffsets(radius: Int): List<Pair<Int, Int>> {
    val reach = radius.coerceAtLeast(0)
    return (-reach..reach).flatMap { dx -> (-reach..reach).map { dz -> dx to dz } }
}

/** Splits [total] items into stack sizes no larger than [maxStackSize], so a merged drop spawns few entities. */
internal fun stackSizes(total: Int, maxStackSize: Int): List<Int> {
    if (total <= 0 || maxStackSize <= 0) return emptyList()
    val fullStacks = List(total / maxStackSize) { maxStackSize }
    val remainder = total % maxStackSize
    return if (remainder == 0) fullStacks else fullStacks + remainder
}

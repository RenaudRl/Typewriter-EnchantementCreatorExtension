package btcrenaud.enchantment

import org.bukkit.Material

interface RegisteredEnchantment {
    val id: String
    val name: String
    val displayName: String
    val nameColor: String
    val supportedItems: List<Material>
    val anvilCost: Int
    val maxLevel: Int
    val weight: Int
    val minimumCost: EnchantmentDefinition.Cost
    val maximumCost: EnchantmentDefinition.Cost
    val enchantmentLore: String
}

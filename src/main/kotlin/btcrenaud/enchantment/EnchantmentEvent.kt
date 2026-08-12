package btcrenaud.enchantment

import org.bukkit.event.Event
import com.typewritermc.core.interaction.GlobalContextKey

object BukkitEventContextKey : GlobalContextKey<Event>(Event::class)
object EnchantmentLevelContextKey : GlobalContextKey<Int>(Int::class)
object EnchantmentSlotContextKey : GlobalContextKey<EnchantSlot>(EnchantSlot::class)
object EnchantmentLevelModeContextKey : GlobalContextKey<EnchantmentLevelMode>(EnchantmentLevelMode::class)

enum class EnchantmentEvent {
    PLAYER_ATTACK,     // When player hits an entity
    PLAYER_DEFEND,     // When player takes damage
    BLOCK_BREAK,       // When player breaks a block
    BOW_SHOOT,         // When player shoots a bow or crossbow
    PROJECTILE_HIT,    // When a shot projectile hits an entity
    ITEM_CONSUME,      // When player eats/drinks an item
    PLAYER_DEATH,      // When player dies
    PLAYER_INTERACT,    // When player interact (right/left click)
    ARMOR_EQUIP,       // When player equips or unequips armor
    ITEM_DAMAGE,       // When an item takes damage (e.g., durability loss)
    FALL_DAMAGE,       // When player takes fall damage
    ENVIRONMENTAL_DAMAGE, // Any non-entity damage (fire, lava, kinetic)
    PLAYER_MOVE,       // When player moves (throttled)
    SHIELD_BLOCK,      // When player blocks an attack with a shield
    SNEAK,             // When player starts sneaking
    SPRINT             // When player starts sprinting
}

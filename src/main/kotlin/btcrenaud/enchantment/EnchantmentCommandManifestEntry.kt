package btcrenaud.enchantment

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.command.dsl.DslCommand
import com.typewritermc.engine.paper.command.dsl.command
import com.typewritermc.engine.paper.command.dsl.int
import com.typewritermc.engine.paper.command.dsl.sender
import com.typewritermc.engine.paper.command.dsl.withPermission
import com.typewritermc.engine.paper.command.dsl.word
import com.typewritermc.engine.paper.entry.ManifestEntry
import com.typewritermc.engine.paper.entry.entries.CustomCommandEntry
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.asMini
import com.typewritermc.engine.paper.utils.server
import io.papermc.paper.command.brigadier.CommandSourceStack
import java.util.Locale

/**
 * Registers the admin command for the enchantment system through the engine's
 * [CustomCommandEntry] pipeline: `/{commandName} give <player> <enchant> [level]`.
 */
@Entry(
    name = "enchantment_command_manifest",
    description = "Admin command for giving custom enchantment books",
    color = Colors.ORANGE,
    icon = "mdi:console-line"
)
@Tags("enchantment", "command", "manifest")
class EnchantmentCommandManifestEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("Name of the admin command")
    val commandName: String = "enchantcreator",
    @Help("Permission required to use the command")
    val adminPermission: String = "enchantcreator.admin",
    @Help("Message sent when the target player is not online")
    val playerNotFound: String = "Player '{player}' is not online.",
    @Help("Message sent when no enchantment matches the given name")
    val enchantmentNotFound: String = "Unknown enchantment '{enchant}'.",
    @Help("Message sent when the book has been given (placeholders: {player}, {enchant}, {level})")
    val bookGiven: String = "Gave {enchant} {level} to {player}.",
) : ManifestEntry, CustomCommandEntry {

    override fun command(): DslCommand<CommandSourceStack> = command(commandName.ifBlank { "enchantcreator" }) {
        withPermission(adminPermission)
        literal("give") {
            word("player") { playerName ->
                word("enchant") { enchantName ->
                    executes { give(sender, playerName(), enchantName(), 1) }
                    int("level", min = 1) { level ->
                        executes { give(sender, playerName(), enchantName(), level()) }
                    }
                }
            }
        }
    }

    private fun give(sender: org.bukkit.command.CommandSender, playerName: String, enchantName: String, level: Int) {
        val target = server.getPlayerExact(playerName)
        if (target == null) {
            sender.sendMessage(playerNotFound.replace("{player}", playerName).asMini())
            return
        }
        val def = findDefinition(enchantName)
        if (def == null) {
            sender.sendMessage(enchantmentNotFound.replace("{enchant}", enchantName).asMini())
            return
        }
        val safeLevel = EnchantmentRuntime.clampLevel(level, def.normalizedMaxLevel())
        target.scheduler.run(plugin, serverTask@{ _ ->
            val book = EnchantmentManager.buildBook(def, safeLevel)
            if (book == null) {
                sender.sendMessage(enchantmentNotFound.replace("{enchant}", enchantName).asMini())
                return@serverTask
            }
            val leftover = target.inventory.addItem(book)
            leftover.values.forEach { target.world.dropItemNaturally(target.location, it) }
            sender.sendMessage(
                bookGiven
                    .replace("{player}", target.name)
                    .replace("{enchant}", def.displayName.ifBlank { def.name })
                    .replace("{level}", safeLevel.toString())
                    .asMini()
            )
        }, null)
    }

    private fun findDefinition(enchantName: String): RegisteredEnchantment? {
        val query = enchantName.lowercase(Locale.ROOT).replace(' ', '_')
        val all = Query.find<EnchantmentDefinition>().toList() + Query.find<CustomEnchantmentDefinition>().toList()
        return all.firstOrNull { def ->
            def.name.lowercase(Locale.ROOT).replace(' ', '_') == query ||
                def.displayName.lowercase(Locale.ROOT).replace(' ', '_') == query
        }
    }
}

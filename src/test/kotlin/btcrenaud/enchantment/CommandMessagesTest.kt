package btcrenaud.enchantment

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandMessagesTest {
    private val miniMessage = MiniMessage.miniMessage()
    private val plain = PlainTextComponentSerializer.plainText()

    private fun displayed(message: String): String = plain.serialize(miniMessage.deserialize(message))

    @Test
    fun `a typed tag is displayed literally`() {
        val message = "Player '{player}' is not online."
            .fillPlaceholders(mapOf("player" to escapeMiniMessage("<red>x")))

        assertEquals("Player '<red>x' is not online.", displayed(message))
    }

    @Test
    fun `a typed tag is not interpreted even when the admin template is colored`() {
        val message = "<red>Unknown enchantment '{enchant}'.</red>"
            .fillPlaceholders(mapOf("enchant" to escapeMiniMessage("<click:run_command:/op me>x")))

        assertEquals("Unknown enchantment '<click:run_command:/op me>x'.", displayed(message))
    }

    @Test
    fun `a template without typed tags renders exactly as before`() {
        val message = "<green>Gave {enchant} {level} to {player}.</green>".fillPlaceholders(
            mapOf(
                "player" to escapeMiniMessage("Steve_01"),
                "enchant" to "<gold>Lifesteal</gold>",
                "level" to "3",
            )
        )

        assertEquals("<green>Gave <gold>Lifesteal</gold> 3 to Steve_01.</green>", message)
        assertEquals("Gave Lifesteal 3 to Steve_01.", displayed(message))
    }

    @Test
    fun `a value is not expanded a second time`() {
        val message = "{player} / {enchant}"
            .fillPlaceholders(mapOf("player" to escapeMiniMessage("{enchant}"), "enchant" to "Sharp"))

        assertEquals("{enchant} / Sharp", displayed(message))
    }

    @Test
    fun `an unknown placeholder is kept as written`() {
        assertEquals("{nobody} Steve", "{nobody} {player}".fillPlaceholders(mapOf("player" to "Steve")))
    }
}

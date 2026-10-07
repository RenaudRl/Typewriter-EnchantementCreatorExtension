package btcrenaud.enchantment

import net.kyori.adventure.text.minimessage.MiniMessage

private val miniMessage = MiniMessage.miniMessage()
private val placeholderPattern = Regex("""\{(\w+)}""")

/**
 * Neutralizes MiniMessage tags in [input], so that text typed by a person is displayed literally
 * once the surrounding message is parsed as MiniMessage.
 *
 * Use it on every value that does not come from an entry field. Values written by the admin in an entry
 * (display names, lore) are meant to carry formatting and must not be escaped.
 */
internal fun escapeMiniMessage(input: String): String = miniMessage.escapeTags(input)

/**
 * Replaces each `{key}` of this message template by its value in [values].
 *
 * Substitution is done in a single pass: a value that itself contains `{other}` is not expanded again.
 * Unknown `{key}` are kept as written. Values are inserted as is; escape the typed ones with [escapeMiniMessage].
 */
internal fun String.fillPlaceholders(values: Map<String, String>): String =
    placeholderPattern.replace(this) { match -> values[match.groupValues[1]] ?: match.value }

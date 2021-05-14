package dev.giuliopime.helpdesk.bot.internals.extentions

fun String.takeFirstN(n: Int = 40): String {
    return if (length > n) take(n - 3) + "..." else this
}

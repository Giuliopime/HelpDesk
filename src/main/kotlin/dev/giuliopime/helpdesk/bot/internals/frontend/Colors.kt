package dev.giuliopime.helpdesk.bot.internals.frontend

import java.awt.Color
import kotlin.random.Random

object Colors {
    val primary: Color = Color.decode("#5865F2")

    val green: Color = Color.decode("#57F287")
    val yellow: Color = Color.decode("#FEE75C")
    val red: Color = Color.decode("#ED4245")

    fun random(): Color {
        val random = Random
        return Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }
}

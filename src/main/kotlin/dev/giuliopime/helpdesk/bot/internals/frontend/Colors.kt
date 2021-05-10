package dev.giuliopime.helpdesk.bot.internals.frontend

import java.awt.Color
import kotlin.random.Random

object Colors {
    val primary: Color = Color.decode("#e741e9")

    val green: Color = Color.decode("#43b581")
    val yellow: Color = Color.decode("#faa619")
    val red: Color = Color.decode("#f14846")

    fun random(): Color {
        val random = Random
        return Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }
}

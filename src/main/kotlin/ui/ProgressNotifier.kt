package ui

import com.github.ajalt.mordant.rendering.TextColors.brightCyan
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.green
import com.github.ajalt.mordant.rendering.TextColors.yellow
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.concurrent.getOrSet

class ProgressNotifier {

    internal val value = ThreadLocal<Float>()

    private val t = Terminal(null)

    fun preReadingModulesMessage() {
        t.println(yellow("Initializing...."))
    }

    fun postReadingModulesMessage() {
        t.println("Processing modules....")
    }

    fun preUpdatingModuleMessage(module: String) {
        t.println(
            "Updating $module's package name${brightCyan("..${value.getOrSet { 0f }}%..")}"
        )
    }

    fun postUpdatingModulesMessage() {
        t.println(cyan("Finishing...."))
    }

    internal fun updateProgress(delta: Float, startNewLine: Boolean = false) {
        val oldVal = value.getOrSet { 0f }
        val newVal = (delta + oldVal).coerceIn(0f, 100f).coerceAtLeast(oldVal)
        value.set(newVal)
        t.print(brightCyan("..$newVal%..."))
    }

    fun successMessage() {
        t.println(green("Updating pkg name succeeded!"))
    }
}
package ui

import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.mordant.rendering.TextColors.brightCyan
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.green
import com.github.ajalt.mordant.rendering.TextColors.yellow
import kotlin.concurrent.getOrSet

class ProgressNotifier {

    internal val value = ThreadLocal<Float>()

    fun preReadingModulesMessage() {
        TermUi.echo(yellow("Initializing...."))
    }

    fun postReadingModulesMessage() {
        TermUi.echo("Processing modules....")
    }

    fun preUpdatingModuleMessage(module: String) {
        TermUi.echo(
            "Updating $module's package name${brightCyan("..${value.getOrSet { 0f }}%..")}",
            trailingNewline = false
        )
    }

    fun postUpdatingModulesMessage() {
        TermUi.echo(cyan("Finishing...."))
    }

    internal fun updateProgress(delta: Float, startNewLine: Boolean = false) {
        val oldVal = value.getOrSet { 0f }
        val newVal = (delta + oldVal).coerceIn(0f, 100f).coerceAtLeast(oldVal)
        value.set(newVal)
        TermUi.echo(brightCyan("..$newVal%..."), trailingNewline = startNewLine)
    }

    fun successMessage() {
        TermUi.echo(green("Updating pkg name succeeded!"))
    }
}
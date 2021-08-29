package ui

import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.underline
import com.github.ajalt.mordant.terminal.Terminal

class ChpkgHelpFormatter : CliktHelpFormatter() {
    private val t = Terminal()

    override fun optionMetavar(option: HelpFormatter.ParameterHelp.Option): String {
        return t.render(rgb("#84807F")(super.optionMetavar(option)))
    }

    override fun renderOptionName(name: String) = t.render(brightYellow(super.renderOptionName(name)))

    override fun renderSectionTitle(title: String): String {
        val sectionStyle = (bold + underline)
        return t.render(sectionStyle(super.renderSectionTitle(title)))
    }
}
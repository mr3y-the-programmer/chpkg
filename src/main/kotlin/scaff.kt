import com.github.ajalt.clikt.core.CliktCommand

class Scaff: CliktCommand(allowMultipleSubcommands = true) {
    override fun run() {
        echo("Welcome to Scaff tool!")
    }
}
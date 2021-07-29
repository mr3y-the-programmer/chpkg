import com.github.ajalt.clikt.core.CliktCommand

class Chpkg: CliktCommand() {
    override fun run() {
        echo("Welcome to Chpkg tool!")
    }
}

class Config: CliktCommand(help = "Configure chpkg global options, like the default project type.") {

    override fun run() {

    }
}
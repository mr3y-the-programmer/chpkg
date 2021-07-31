import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.*

class PkgOptions: OptionGroup() {
    val from by option("--from", help = helpMessageFor("old"), metavar = "old package name")
        .convert {
            // for more flexibility, allow pkg name to have a suffix of only 1 "."
            // so, users can in practice use "com.example.app" & "com.example.app." interchangeably
            it.trimLastDot()
        }
        .required()
        .check(badPkgName()) {
            it.isPackageName()
        }

    val to by option("--to", help = helpMessageFor("new"), metavar = "new package name")
        .convert {
            it.trimLastDot()
        }
        .required()
        .validate {
            require(it != from) {
                "--from value is exactly the same as --to value"
            }
            require(it.isPackageName()) {
                badPkgName()
            }
        }

    private fun badPkgName() = "Make sure to specify a valid package name"

    private fun helpMessageFor(param: String): String {
        return "The fully-qualified $param package name or a segment of it, " +
            "so, it can be: \"com.example.app\" or just \"com\" if you want to substitute the first segment only."
    }
}

class Chpkg: CliktCommand(invokeWithoutSubcommand = true) {
    val pkgMetaData by PkgOptions().cooccurring()

    override fun run() {
        echo("Welcome to Chpkg tool!")
    }

}

class Config: CliktCommand(help = "Configure chpkg global options, like the default project type.") {

    override fun run() {

    }
}
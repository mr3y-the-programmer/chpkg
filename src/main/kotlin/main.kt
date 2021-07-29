import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
    Chpkg().subcommands(Config()).main(args)
}
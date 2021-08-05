import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.*
import okio.IOException
import okio.buffer
import okio.source
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.jvm.Throws

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

class Chpkg: CliktCommand(invokeWithoutSubcommand = true, printHelpOnEmptyArgs = true) {
    val pkgOptions by PkgOptions().cooccurring()

    init {
        // TODO: fetch this from git tags
        versionOption("1.0.3", help = "Current Chpkg's version")
    }

    override fun run() {
        val options = pkgOptions ?: return
        val (from, to) = options.from to options.to
        val rootProject = System.getProperty("user.dir")
        fun path(fileName: String): Path {
            return Paths.get(rootProject, fileName).toAbsolutePath()
        }
        val settingsPath = when {
            path("settings.gradle.kts").exists() -> path("settings.gradle.kts")
            path("settings.gradle").exists() -> path("settings.gradle")
            else -> throw UsageError("chpkg cannot function without settings.gradle/.kts file," +
                " make sure you have one in the root of your project")
        }
        val modules = readModulesNames(File(settingsPath.toString()))
        modules.forEach { module ->
            val srcDir = (path(module) / "src" / "main").toString()
            var dirsNotProcessed = Int.MAX_VALUE
            val updatedPaths = mutableListOf<Pair<Path, String>>()
            val sourceFiles = File(srcDir).walkBottomUp()
            for (file in sourceFiles) {
                if (file.isDirectory) {
                    val dirName = file.name
                    val newName = when {
                        dirName == from -> to
                        from.contains(dirName) -> {
                            val bijection = from.split('.') zip to.split('.')
                            fun List<Pair<String, String>>.normalize() = if (from.containsMoreThanOnce(dirName)) {
                                val copy = dirsNotProcessed.coerceIn(0, size)
                                dirsNotProcessed = subList(0, copy).indexOfLast { it.first == dirName }
                                dropLast(size - copy)
                            } else this
                            bijection.normalize().last { it.first == dirName }.second
                        }
                        dirName.contains(from) -> dirName.replace(from, to)
                        else -> continue
                    }
                    updatedPaths += Pair(file.toPath(), newName)
                }
                if (file.isFile) {
                    if (file.extension == "kt" || file.extension == "java") {
                        val parent = file.toPath().parent.name
                        val oldPkgNameRgx = Regex("""package ([a-z0-9_]+\.)*$parent""")
                        when {
                            from == parent -> {
                                val prepend =
                                    oldPkgNameRgx.find(file.readText())?.value?.substringBeforeLast(parent) ?: continue
                                file.replace("$prepend$from", "$prepend$to")
                            }
                            from.contains(parent) -> {
                                if (from.containsMoreThanOnce(parent)) {
                                    // the regex is non-greedy, so it'll match all occurrences
                                    val oldPkg = oldPkgNameRgx.find(file.readText())?.value ?: continue
                                    val prepend = oldPkg.substringBefore(from)
                                    val pkgLength = oldPkg.substringAfter(prepend).split('.').size
                                    file.replace(oldPkg, "$prepend${to.split('.').take(pkgLength)}")
                                } else {
                                    val prefixLength = from.substringBefore(parent).count { it == '.' }
                                    val toSplitted = to.split('.').take(prefixLength + 1)
                                    val (prepend, newParent) = toSplitted.take(prefixLength)
                                        .joinToString(".", postfix = ".") to toSplitted.last()
                                    file.replace(oldPkgNameRgx, "package $prepend$newParent")
                                }
                            }
                            parent.contains(from) -> {
                                val oldPkg = oldPkgNameRgx.find(file.readText())?.value ?: continue
                                file.replace(oldPkg, oldPkg.replace(from, to))
                            }
                            else -> continue
                        }
                    }
                    if (file.name == "AndroidManifest.xml") {
                        val manifestRgx = Regex("""package="([a-z0-9_]+\.?)+"""")
                        val pkg = manifestRgx.find(file.readText())?.value
                                ?.removePrefix("package=")?.removeSurrounding("\"") ?: continue
                        if (pkg == from) {
                            file.replace(pkg, to)
                            continue
                        }
                        val (fromSplitted, toSplitted) = from.split('.') to to.split('.')
                        val newPkg = pkg.split('.').map { pkgSegment ->
                            fromSplitted.forEachIndexed { index, fromSegment ->
                                if (pkgSegment == fromSegment) return@map toSplitted[index]
                            }
                            pkgSegment
                        }.joinToString(separator = ".")
                        file.replace(pkg, newPkg)
                    }
                }
            }
            updatedPaths.forEach { (oldPath, newName) ->
                // TODO: allow user to replace existing dir through a flag
                runCatching { Files.move(oldPath, oldPath.resolveSibling(newName)) }.getOrThrow()
            }
        }
        echo(settingsPath.toString())
    }

    @Throws(IOException::class)
    private fun readModulesNames(settingsFile: File): List<String> {
        val modules = mutableListOf<String>()
        settingsFile.source().buffer().use { source ->
            generateSequence { source.readUtf8Line() }
                .filter(String::isModuleName)
                .map(String::trimInclude)
                .forEach{ moduleName -> modules.add(moduleName) }
        }
        return modules
    }

}

class Config: CliktCommand(help = "Configure chpkg global options, like the default project type.") {

    override fun run() {

    }
}
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.options.versionOption
import okio.IOException
import okio.buffer
import okio.source
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.jvm.Throws

class PkgOptions : OptionGroup() {
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

class Chpkg : CliktCommand(invokeWithoutSubcommand = true, printHelpOnEmptyArgs = true) {
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
            else -> throw UsageError(
                "chpkg cannot function without settings.gradle/.kts file," +
                    " make sure you have one in the root of your project"
            )
        }
        val modules = readModulesNames(File(settingsPath.toString()))
        modules.forEach { module ->
            val srcDir = (path(module) / "src" / "main").toString()
            File(srcDir).walkBottomUp().forEach { file ->
                when {
                    file.isDirectory -> file.updateDirName(from, to)
                    file.isFile -> file.updatePkgName(from, to)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun readModulesNames(settingsFile: File): List<String> {
        val modules = mutableListOf<String>()
        settingsFile.source().buffer().use { source ->
            generateSequence { source.readUtf8Line() }
                .filter(String::isModuleName)
                .map(String::trimInclude)
                .forEach { moduleName -> modules.add(moduleName) }
        }
        return modules
    }
}
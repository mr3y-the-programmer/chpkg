import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.*
import okio.IOException
import okio.buffer
import okio.source
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
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
            val sourceFiles = File(srcDir).walkBottomUp()
            for (file in sourceFiles) {
                if (file.isDirectory) {
                    file.updateDirName(from, to)
                }
                if (file.isFile) {
                    val oldPkg = when {
                        file.extension == "kt" || file.extension == "java" -> {
                            srcPkgNameRgx.find(file.readText())?.value?.removePrefix("package ")
                                ?.removeSuffix(";") ?: continue
                        }
                        file.name == "AndroidManifest.xml" -> {
                            manifestRgx.find(file.readText())?.value
                                ?.removePrefix("package=")?.removeSurrounding("\"") ?: continue
                        }
                        else -> continue
                    }
                    file.updatePkgName(oldPkg, from, to)
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
                .forEach{ moduleName -> modules.add(moduleName) }
        }
        return modules
    }
}

typealias IllegalReceiverException = IllegalArgumentException

@TestOnly
internal fun File.updateDirName(from: String, to: String) {
    if (!isDirectory) throw IllegalReceiverException("$name isn't a valid directory!")
    val dirName = name
    if(!from.startsWith(dirName)) {
        var startBoundFile = parentFile.toPath()
        var startBound = startBoundFile.last().name
        while (startBound.isNotEmpty()) {
            when {
                from.startsWith(startBound) -> break
                from.contains(".") && !from.contains(startBound) -> return
            }
            startBoundFile = startBoundFile.parent
            startBound = startBoundFile.last().name
        }
    }
    val newName = when {
        dirName == from -> to
        from.contains(dirName) -> {
            val bijection = from.split('.') zip to.split('.')
            bijection.dropDuplicates(from, dirName).last { it.first == dirName }.second
        }
        dirName.contains(from) -> dirName.replace(from, to)
        else -> return
    }
    val oldPath = this.toPath()
    // TODO: allow user to replace existing dir through a flag
    runCatching { Files.move(oldPath, oldPath.resolveSibling(newName)) }.getOrThrow()
}

private val dirsNotProcessed = AtomicInteger(Int.MAX_VALUE)

private fun List<Pair<String, String>>.dropDuplicates(container: String, contained: String): List<Pair<String, String>> {
    return if (container.containsMoreThanOnce(contained)) {
        var copy: Int
        var safeCopy: Int
        while (true) {
            copy = dirsNotProcessed.get()
            safeCopy = copy.coerceIn(0, size)
            val newValue = subList(0, safeCopy).indexOfLast { it.first == contained }
            if (dirsNotProcessed.compareAndSet(copy, newValue)) break
        }
        dropLast(size - safeCopy)
    } else this
}

@TestOnly
internal fun File.updatePkgName(oldPkg: String, from: String, to: String) {
    if (from == oldPkg) {
        replace(oldPkg, to)
        return
    }
    // otherwise, we have to go segment by segment
    val (fromSplitted, toSplitted) = from.split('.') to to.split('.')
    var segmentsProcessed = 0
    val newPkg = oldPkg.split('.').map { pkgSegment ->
        val (fromNormalized, itemsDropped, processed, isDuplicate) = fromSplitted.dropHandledDuplicates(pkgSegment, segmentsProcessed)
        segmentsProcessed = processed
        fromNormalized.forEachIndexed { index, fromSegment ->
            if (pkgSegment == fromSegment) return@map if(isDuplicate) toSplitted.drop(itemsDropped)[index] else toSplitted[index]
        }
        pkgSegment
    }.joinToString(separator = ".")
    replace(oldPkg, newPkg)
}

private fun List<String>.dropHandledDuplicates(target: String, segProcessed: Int): Quadratic<List<String>, Int, Int, Boolean> {
    return if (count { it == target } > 1) {
        val copy = segProcessed.coerceAtMost(lastIndex)
        val segmentsProcessed = subList(copy, size).indexOfFirst { it == target } + 1
        Quadratic(drop(copy), copy, segmentsProcessed, true)
    } else {
        Quadratic(this, 0, segProcessed, false)
    }
}
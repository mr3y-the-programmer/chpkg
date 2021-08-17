import okio.buffer
import okio.source
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.name

typealias IllegalReceiverException = IllegalArgumentException

@TestOnly
internal fun File.updateDirName(from: String, to: String) {
    if (!isDirectory) throw IllegalReceiverException("$name isn't a valid directory!")
    val dirName = name
    if (!from.startsWith(dirName)) {
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

private fun List<Pair<String, String>>.dropDuplicates(
    container: String,
    contained: String
): List<Pair<String, String>> {
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
internal fun File.updatePkgName(from: String, to: String) {
    val oldPkg = extractPkgName() ?: return
    val newPkg = if (from == oldPkg) to else oldPkg.replace(from, to)
    replace(oldPkg, newPkg)
}

@TestOnly
internal fun File.extractPkgName(): String? {
    return when {
        extension == "kt" || extension == "java" -> {
            bufferedSequence(
                filter = { line ->
                    line.matches(srcPkgNameRgx)
                },
                map = { line ->
                    line.removePrefix("package ").removeSuffix(";")
                }
            )
        }
        name == "AndroidManifest.xml" -> {
            // package in manifest file cannot be detected easily using okio approach,
            // so keep using the old approach here
            manifestRgx.find(readText())?.value
                ?.removePrefix("package=")?.removeSurrounding("\"")
        }
        else -> null
    }
}

private fun File.bufferedSequence(filter: (String) -> Boolean, map: (String) -> String) =
    source().buffer().use { bufferedSource ->
        generateSequence { bufferedSource.readUtf8Line() }
            .filter { filter(it) }
            .map { map(it) }
            .singleOrNull()
    }
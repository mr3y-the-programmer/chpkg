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
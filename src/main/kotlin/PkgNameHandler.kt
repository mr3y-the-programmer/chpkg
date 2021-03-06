
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.buffer
import okio.source
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.nio.file.Files
import kotlin.concurrent.getOrSet
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.path.name

typealias IllegalReceiverException = IllegalArgumentException

@TestOnly
internal suspend fun File.updateDirName(from: String, to: String) = suspendCancellableCoroutine<Unit> { cont ->
    if (!isDirectory) cont.resumeWithException(IllegalReceiverException("$name isn't a valid directory!"))
    val dirName = name
    while (cont.isActive) {
        if (!from.startsWith(dirName)) {
            var startBoundFile = parentFile.toPath()
            var startBound = startBoundFile.last().name
            while (startBound.isNotEmpty()) {
                when {
                    from.startsWith(startBound) -> break
                    from.contains(".") && !from.contains(startBound) -> {
                        cont.resume(Unit)
                        return@suspendCancellableCoroutine
                    }
                }
                startBoundFile = startBoundFile.parent
                startBound = startBoundFile.lastOrNull()?.name ?: break
            }
        }
        val newName = when {
            dirName == from -> to
            from.contains(dirName) -> {
                val bijection = from.split('.') zip to.split('.')
                bijection.dropDuplicates(from, dirName).last { it.first == dirName }.second
            }
            dirName.contains(from) -> dirName.replace(from, to)
            else -> {
                cont.resume(Unit)
                return@suspendCancellableCoroutine
            }
        }
        val oldPath = this.toPath()
        // TODO: allow user to replace existing dir through a flag
        runCatching { Files.move(oldPath, oldPath.resolveSibling(newName)) }.getOrElse { cont.resumeWithException(it) }
        cont.resume(Unit)
    }
}

// This should be private but for correctness, coroutines' job needs to know about
// it, so as a workaround it is internal for now
internal val dirsNotProcessed = ThreadLocal<Int>()

private fun List<Pair<String, String>>.dropDuplicates(
    container: String,
    contained: String
): List<Pair<String, String>> {
    return if (container.containsMoreThanOnce(contained)) {
        /*runBlocking {
            dirsNotProcessed.ensurePresent()
        }*/
        val copy = dirsNotProcessed.getOrSet { size }.coerceIn(0, size)
        dirsNotProcessed.set(subList(0, copy).indexOfLast { it.first == contained })
        dropLast(size - copy)
    } else this
}

@TestOnly
internal suspend fun File.updatePkgName(from: String, to: String) = suspendCancellableCoroutine<Unit> { cont ->
    while (cont.isActive) {
        val oldPkg = extractPkgName()
        if (oldPkg != null) {
            val newPkg = if (from == oldPkg) to else oldPkg.replace(from, to)
            replace(oldPkg, newPkg)
            cont.resume(Unit)
        } else {
            cont.resume(Unit)
            return@suspendCancellableCoroutine
        }
    }
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
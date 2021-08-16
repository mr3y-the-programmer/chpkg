package utils

import java.io.File
import java.nio.file.Paths
import kotlin.io.path.div

internal val root = Paths.get("").toAbsolutePath() / "src" / "test" / "fakeproject"

internal fun getModuleMainFiles(module: String): Sequence<File> {
    val srcDir = root / module / "src" / "main"
    return File(srcDir.toString()).walkBottomUp().filter { it.isFile }
}
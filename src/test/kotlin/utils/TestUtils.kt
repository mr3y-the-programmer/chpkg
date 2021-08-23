package utils

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.div

internal val root = Paths.get("").toAbsolutePath() / "src" / "test" / "fakeproject"

private val srcDir: (String) -> Path = { module -> root / module / "src" / "main" }

internal val baseMainPath: (String) -> Path = { module -> root / module / "src" / "main" }

internal val baseTestPath: (String) -> Path = { module -> root / module / "src" / "test" }

internal fun getModuleMainFiles(module: String): Sequence<File> {
    return File(srcDir(module).toString()).walkBottomUp().filter { it.isFile }
}

internal fun getModuleMainDirs(module: String): Sequence<File> {
    return File(srcDir(module).toString()).walkBottomUp().filter { it.isDirectory }
}

internal fun getModuleTestDirs(module: String): Sequence<File> {
    return File(baseTestPath(module).toString()).walkBottomUp().filter { it.isDirectory }
}
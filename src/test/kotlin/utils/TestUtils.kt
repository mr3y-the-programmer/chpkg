package utils

import java.nio.file.Paths
import kotlin.io.path.div

internal val root = Paths.get("").toAbsolutePath() / "src" / "test" / "fakeproject"
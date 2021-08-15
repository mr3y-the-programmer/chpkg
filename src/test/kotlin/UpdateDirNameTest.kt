import org.junit.Test
import utils.root
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.test.assertTrue

/*
    Check dir names are updated correctly
 */
class UpdateDirNameTest {

    @Test
    fun `check base module directories are updated correctly`() {
        val (from, to) = "com.project" to "net.bar"
        val baseModulePath = root / "base" / "src" / "main" / "kotlin"
        val dirs = File(baseModulePath.toString()).walkBottomUp().filter { it.isDirectory }
        dirs.forEach { it.updateDirName(from, to) }
        val (oldPath, newPath) = Path("com") / "project" / "base" to Path("net") / "bar" / "base"
        assertTrue { (baseModulePath / newPath / "project").exists() }
        assertTrue { (baseModulePath / newPath / "util").exists() }
        assertTrue { (baseModulePath / oldPath / "project").notExists() }
        assertTrue { (baseModulePath / oldPath / "util").notExists() }
        // reset to default
        dirs.forEach { it.updateDirName(to, from) }
    }
}
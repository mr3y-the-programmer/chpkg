import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import utils.TestCoroutineRule
import utils.baseMainPath
import utils.getModuleMainDirs
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.test.assertTrue

/*
    Check dir names are updated correctly
 */
@ExperimentalCoroutinesApi
class UpdateDirNameTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @Test
    fun `check app module directories are updated correctly`() {
        val oldPath = baseMainPath("app") / "kotlin" / "com" / "project" / "app"
        val newPath = baseMainPath("app") / "kotlin" / "net" / "bar" / "foo"
        runTest("app", "com.project.app", "net.bar.foo", oldPath, newPath)
    }

    @Test
    fun `check base module parent dirs are updated correctly`() {
        val oldPath = baseMainPath("base") / "kotlin" / "com" / "project" / "base" / "project"
        val newPath = baseMainPath("base") / "kotlin" / "io" / "foo" / "base" / "project"
        runTest("base", "com.project", "io.foo", oldPath, newPath)
    }

    @Test
    fun `check base module single duplicated segment is updated in multiple places`() {
        val oldPath = baseMainPath("base") / "kotlin" / "com" / "project" / "base" / "project"
        val newPath = baseMainPath("base") / "kotlin" / "com" / "foo" / "base" / "foo"
        runTest("base", "project", "foo", oldPath, newPath)
    }

    @Test
    fun `check base module multiple duplicated segment is mapped correctly`() {
        val oldPath = baseMainPath("base") / "kotlin" / "com" / "project" / "base" / "project"
        val newPath = baseMainPath("base") / "kotlin" / "com" / "pro" / "foo" / "io"
        runTest("base", "project.base.project", "pro.foo.io", oldPath, newPath)
    }

    @Test
    fun `check common module's part of dir is updated correctly`() {
        val oldPath = baseMainPath("common") / "kotlin" / "com" / "project" / "common" / "app"
        val newPath = baseMainPath("common") / "kotlin" / "com" / "prcrowct" / "common" / "app"
        runTest("common", "oje", "crow", oldPath, newPath)
    }

    private fun runTest(
        module: String,
        from: String,
        to: String,
        oldPath: Path,
        newPath: Path
    ) = coroutineRule.testDispatcher.runBlockingTest {
        val dirs = getModuleMainDirs(module)
        dirs.forEach { it.updateDirName(from, to) }
        assertTrue { newPath.exists() }
        assertTrue { newPath.exists() }
        assertTrue { oldPath.notExists() }
        assertTrue { oldPath.notExists() }
        // reset to default
        dirs.forEach { it.updateDirName(to, from) }
    }
}
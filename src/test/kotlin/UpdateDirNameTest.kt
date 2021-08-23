import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import utils.TestCoroutineRule
import utils.getModuleMainDirs
import utils.getModuleTestDirs
import utils.srcMainPath
import utils.srcTestPath
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
        val oldPath = srcMainPath("app") / "kotlin" / "com" / "project" / "app"
        val newPath = srcMainPath("app") / "kotlin" / "net" / "bar" / "foo"
        runTest("app", "com.project.app", "net.bar.foo", oldPath, newPath)
    }

    @Test
    fun `check base module parent dirs are updated correctly`() {
        val oldPath = srcMainPath("base") / "kotlin" / "com" / "project" / "base" / "project"
        val newPath = srcMainPath("base") / "kotlin" / "io" / "foo" / "base" / "project"
        runTest("base", "com.project", "io.foo", oldPath, newPath)
    }

    @Test
    fun `check base module single duplicated segment is updated in multiple places`() {
        val oldPath = srcMainPath("base") / "kotlin" / "com" / "project" / "base" / "project"
        val newPath = srcMainPath("base") / "kotlin" / "com" / "foo" / "base" / "foo"
        runTest("base", "project", "foo", oldPath, newPath)
    }

    @Test
    fun `check base module multiple duplicated segment is mapped correctly`() {
        val oldPath = srcMainPath("base") / "kotlin" / "com" / "project" / "base" / "project"
        val newPath = srcMainPath("base") / "kotlin" / "com" / "pro" / "foo" / "io"
        runTest("base", "project.base.project", "pro.foo.io", oldPath, newPath)
    }

    @Test
    fun `check base module multiple duplicated segment in two different paths are updated correctly`() {
        val oldMainPath = srcMainPath("base") / "kotlin" / "com" / "project" / "base" / "project"
        val newMainPath = srcMainPath("base") / "kotlin" / "com" / "pro" / "foo" / "io"
        val oldTestPath = srcTestPath("base") / "kotlin" / "com" / "project" / "base" / "project"
        val newTestPath = srcTestPath("base") / "kotlin" / "com" / "pro" / "foo" / "io"
        coroutineRule.testDispatcher.runBlockingTest {
            launch(dirsNotProcessed.asContextElement(value = Int.MAX_VALUE)) {
                runTest("base", "project.base.project", "pro.foo.io", oldMainPath, newMainPath)
            }
            launch(dirsNotProcessed.asContextElement(value = Int.MAX_VALUE)) {
                runTest("base", "project.base.project", "pro.foo.io", oldTestPath, newTestPath, true)
            }
        }
    }

    @Test
    fun `check common module's part of dir is updated correctly`() {
        val oldPath = srcMainPath("common") / "kotlin" / "com" / "project" / "common" / "app"
        val newPath = srcMainPath("common") / "kotlin" / "com" / "prcrowct" / "common" / "app"
        runTest("common", "oje", "crow", oldPath, newPath)
    }

    private fun runTest(
        module: String,
        from: String,
        to: String,
        oldPath: Path,
        newPath: Path,
        isTestPath: Boolean = false
    ) = coroutineRule.testDispatcher.runBlockingTest {
        val dirs = if (isTestPath) getModuleTestDirs(module) else getModuleMainDirs(module)
        dirs.forEach { it.updateDirName(from, to) }
        assertTrue { newPath.exists() }
        assertTrue { newPath.exists() }
        assertTrue { oldPath.notExists() }
        assertTrue { oldPath.notExists() }
        // reset to default
        dirs.forEach { it.updateDirName(to, from) }
    }
}
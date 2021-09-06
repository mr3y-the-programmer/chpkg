import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import utils.TestCoroutineRule
import utils.getModuleMainFiles
import utils.root
import java.io.File
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ChpkgRunTest {

    @Test
    fun `change the global namespace from com,project to net,bar`() {
        val (oldNameSpace, newNameSpace) = "com.project" to "net.bar"
        cmd("$oldNameSpace" "$newNameSpace")
        val (modulesPaths, moduleFiles) = modules.map { root / it } to modules.flatMap { getModuleMainFiles(it) }
        modulesPaths.forEach { path ->
            val src = path / "src" / "main" / "kotlin"
            val (oldDir, newDir) = src / "com" / "project" to src / "net" / "bar"
            assertTrue { oldDir.notExists() }
            assertTrue { newDir.exists() }
        }
        moduleFiles.forEach { file ->
            assertTrue { file.readText().contains(newNameSpace) }
            assertFalse { file.readText().contains(oldNameSpace) }
        }
        // reset to default to make the test idempotent & deterministic
        cmd("$newNameSpace" "$oldNameSpace")
    }

    private fun cmd(command: String) = chpkg.parse(command.split(' ').toTypedArray())

    companion object {
        private lateinit var chpkg: Chpkg
        private val modules = getModules()
        @get:Rule
        val coroutineRule = TestCoroutineRule()

        @BeforeClass
        @JvmStatic
        fun initAndHeadToRootProject() {
            chpkg = Chpkg(coroutineRule.testDispatcher)
            System.setProperty("user.dir", root.toString())
        }

        private fun getModules(): List<String> {
            return File(root.toString())
                .listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?.toList() ?: emptyList()
        }
    }
}
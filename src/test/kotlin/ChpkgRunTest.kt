import org.junit.BeforeClass
import org.junit.Test
import utils.root
import java.io.File
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.test.assertTrue

class ChpkgRunTest {

    @Test
    fun `change the global namespace from com,project to net,bar`() {
        val (oldNameSpace, newNameSpace) = "com.project" to "net.bar"
        cmd("--from $oldNameSpace --to $newNameSpace")
        val modulesPaths = modules.map { root / it }
        modulesPaths.forEach { path ->
            val src = path / "src" / "main" / "kotlin"
            val (oldDir, newDir) = src / "com" / "project" to src / "net" / "bar"
            assertTrue { oldDir.notExists() }
            assertTrue { newDir.exists() }
        }
        // reset to default to make the test idempotent & deterministic
        cmd("--from $newNameSpace --to $oldNameSpace")
    }

    private fun cmd(command: String) = chpkg.parse(command.split(' ').toTypedArray())

    companion object {
        private lateinit var chpkg: Chpkg
        private val modules = getModules()

        @BeforeClass
        @JvmStatic
        fun initAndHeadToRootProject() {
            chpkg = Chpkg()
            System.setProperty("user.dir", root.toString())
        }

        private fun getModules(): List<String> {
            return File(root.toString()).listFiles()?.filter { it.isDirectory }?.map { it.name }?.toList() ?: emptyList()
        }
    }
}
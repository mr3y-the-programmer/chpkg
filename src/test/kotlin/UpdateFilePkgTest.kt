import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import utils.getModuleMainFiles
import kotlin.test.assertFalse
import kotlin.test.assertTrue

data class ModuleWithPkgName(
    val moduleName: String,
    val oldName: String,
    val newName: String?
)

@RunWith(Enclosed::class)
class UpdateFilePkgTest {

    /*
        Check Pkg name is extracted correctly
     */
    @RunWith(Parameterized::class)
    class ExtractPkgNameTest(private val moduleMetadata: ModuleWithPkgName) {
        @Test
        fun `check pkg name is extracted correctly`() {
            val (module, pkgName) = moduleMetadata
            getModuleMainFiles(module).forEach { file ->
                val actual = file.extractPkgName()
                assertTrue { actual?.startsWith(pkgName) ?: false }
            }
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun testData(): List<Array<ModuleWithPkgName>> {
                return listOf(
                    arrayOf(ModuleWithPkgName("app", "com.project.app", null)),
                    arrayOf(ModuleWithPkgName("base", "com.project.base", null)),
                    arrayOf(ModuleWithPkgName("base-android", "com.project", null)),
                    arrayOf(ModuleWithPkgName("common", "com.project.common", null)),
                    arrayOf(ModuleWithPkgName("core", "com.project.core", null)),
                    arrayOf(ModuleWithPkgName("model", "com.project.model", null)),
                )
            }
        }
    }

    /*
        Check file's package name is updated correctly
     */
    @RunWith(Parameterized::class)
    class UpdatePkgNameTest(private val moduleMetadata: ModuleWithPkgName) {

        @Test
        fun `check module's package name is updated correctly`() {
            val (module, from, to) = moduleMetadata
            val files = getModuleMainFiles(module)
            files.forEach { file ->
                file.updatePkgName(from, to!!)
                val content = file.readText()
                assertTrue { content.contains(to) }
                assertFalse { content.contains(from) }
            }
            // reset to default
            files.forEach { it.updatePkgName(to!!, from) }
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun testData(): List<Array<ModuleWithPkgName>> {
                return listOf(
                    arrayOf(ModuleWithPkgName("app", "com.project.app", "io.bar.foo")),
                    arrayOf(ModuleWithPkgName("base-android", "com.project", "new.crow")),
                    arrayOf(ModuleWithPkgName("base", "com.project.base", "new.arrow.make")),
                    arrayOf(ModuleWithPkgName("common", "com", "net")),
                    arrayOf(ModuleWithPkgName("core", "project.core", "app.internal")),
                    arrayOf(ModuleWithPkgName("model", "oje", "brew")),
                )
            }
        }
    }
}
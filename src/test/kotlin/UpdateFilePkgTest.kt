import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import utils.getModuleMainFiles
import kotlin.test.assertFalse
import kotlin.test.assertTrue

data class ModuleWithPkgName(
    val moduleName: String,
    val pkgName: String
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
                    arrayOf(ModuleWithPkgName("app", "com.project.app")),
                    arrayOf(ModuleWithPkgName("base", "com.project.base")),
                    arrayOf(ModuleWithPkgName("base-android", "com.project")),
                    arrayOf(ModuleWithPkgName("common", "com.project.common")),
                    arrayOf(ModuleWithPkgName("core", "com.project.core")),
                    arrayOf(ModuleWithPkgName("model", "com.project.model")),
                )
            }
        }
    }

    /*
        Check file's package name is updated correctly
     */
    @RunWith(Parameterized::class)
    class UpdatePkgNameTest(private val oldToNew: Pair<String, String>) {

        @Test
        fun `check app module's package name is updated correctly`() {
            val (from, to) = oldToNew
            val files = getModuleMainFiles("app")
            files.forEach { file ->
                file.updatePkgName(from, to)
                val content = file.readText()
                assertTrue { content.contains(to) }
                assertFalse { content.contains(from) }
            }
            // reset to default
            files.forEach { it.updatePkgName(to, from) }
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun testData(): List<Array<Pair<String, String>>> {
                return listOf(
                    arrayOf(Pair("com.project", "io.bar")),
                    arrayOf(Pair("com.project.app", "new.oroo.crow")),
                    arrayOf(Pair("com.project", "bar.bar")),
                )
            }
        }
    }
}
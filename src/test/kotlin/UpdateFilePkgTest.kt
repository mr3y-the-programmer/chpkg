import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import utils.root
import java.io.File
import kotlin.io.path.div
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private typealias PkgMetaData = Quadratic<String, String, String, String>

/*
    Check file's package name is updated correctly
 */
@RunWith(Parameterized::class)
class UpdateFilePkgTest(private val data: PkgMetaData) {

    @Test
    fun `check app module's package name is updated correctly`() {
        val pkg = "com.project.app"
        val (from, to, newPkg, segmentsUnchanged) = data
        val appModulePath = root / "app" / "src" / "main"
        val manifestFile = File((appModulePath / "AndroidManifest.xml").toString())
        val javaFile = File((appModulePath / "kotlin" / "com" / "project" / "app" / "Foo.java").toString())
        val ktFile = File((appModulePath / "kotlin" / "com" / "project" / "app" / "Foo.kt").toString())

        val files = listOf(manifestFile, javaFile, ktFile)
        files.forEach {
            it.updatePkgName(pkg, from, to)
            val content = it.readText()
            assertTrue { content.contains("$to$segmentsUnchanged") }
            assertFalse { content.contains("$from$segmentsUnchanged") }
        }
        // reset to default
        files.forEach { it.updatePkgName(newPkg, to, from) }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun testData(): List<Array<PkgMetaData>> {
            return listOf(
                arrayOf(PkgMetaData("com.project", "io.bar", "io.bar.app", ".app")),
                arrayOf(PkgMetaData("com.project.app", "new.oroo.crow", "new.oroo.crow", "")),
                arrayOf(PkgMetaData("com.project", "bar.bar", "bar.bar.app", ".app")),
            )
        }
    }
}
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*
    validate the package name.
 */
class PkgValidatorTest {

    @Test
    fun `check for invalid package names`() {
        assertFalse { "com.exa    ple.app".isPackageName() }
        assertFalse { "com.example.app..".isPackageName() }
        assertFalse { "com..example.app".isPackageName() }
        assertFalse { "com..exa?\\}{;mple.app".isPackageName() }
        assertFalse { "com..exa~@#\$()mple.app".isPackageName() }
        assertFalse { "com.ex*&^ample.app".isPackageName() }
        assertFalse { "com.example.app.".isPackageName() }
        assertFalse { "com.exa0mp9le.a_p_p.".isPackageName() }
        assertFalse { "com.".isPackageName() }
    }

    @Test
    fun `check for valid package names`() {
        assertTrue { "com.example.app".isPackageName() }
        assertTrue { "com".isPackageName() }
        assertTrue { "comexample".isPackageName() }
        assertTrue { "com.exa0mp9le.a_p_p".isPackageName() }
    }


}
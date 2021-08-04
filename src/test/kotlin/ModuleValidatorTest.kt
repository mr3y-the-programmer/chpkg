import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModuleValidatorTest {

    @Test
    fun `check for invalid module names`() {
        assertFalse { "include ':base-andr*oid'".isModuleName() }
        assertFalse { "include ':base-andr oid'".isModuleName() }
        assertFalse { "include \":base-andr*oid\"".isModuleName() }
        assertFalse { "include \":base-andr oid\"".isModuleName() }
        assertFalse { "include(\":base-andr*oid\")".isModuleName() }
        assertFalse { "include(\":base-andr oid\")".isModuleName() }

        // TODO: modify your regex to account for those cases
//        assertFalse { "include \":-android\"".isModuleName() }
//        assertFalse { "include ':-android'".isModuleName() }
//        assertFalse { "include(\":-android\")".isModuleName() }
    }

    @Test
    fun `check for valid module names`() {
        assertTrue { "include \":common-ui-resources\"".isModuleName() }
        assertTrue { "include ':common-ui-resources'".isModuleName() }
        assertTrue { "include(\":app\")".isModuleName() }
        assertTrue { "include(\":base-android\")".isModuleName() }
    }

    @Test
    fun `verify that modules are trimmed correctly`() {
        val expected = "app"
        val moduleInKts = "include(\":app\")".trimInclude()
        assertEquals(expected, moduleInKts)
        val moduleInSingleQuote = "include ':app'".trimInclude()
        assertEquals(expected, moduleInSingleQuote)
        val moduleInDoubleQuote = "include \":app\"".trimInclude()
        assertEquals(expected, moduleInDoubleQuote)
    }
}
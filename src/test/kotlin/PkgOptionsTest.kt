import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.MissingOption
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// Or use JUnitCore as Jake: https://github.com/JakeWharton/adb-event-mirror/blob/trunk/adb-event-mirror.main.kts
class PkgOptionsTest {

    @Test
    fun `entering from without to should throw MissingOption`() {
        assertFailsWith(MissingOption::class) {
            cmd("com.example.com")
        }
    }

    @Test
    fun `entering to without from should throw MissingOption`() {
        assertFailsWith(MissingOption::class) {
            cmd("--to com.example.com")
        }
    }

    @Test
    fun `to value is the same as from value should throw BadParameterValue`() {
        assertFailsWith(BadParameterValue::class) {
            cmd("com.example.com" "com.example.com")
        }
        assertFailsWith(BadParameterValue::class) {
            cmd("com.example.com." "com.example.com")
        }
        assertFailsWith(BadParameterValue::class) {
            cmd("com.example.com" "com.example.com.")
        }
    }

    @Test
    fun `from & to don't match on the number of segments should throw BadParameterValue`() {
        assertFailsWith(BadParameterValue::class) {
            cmd("com.example.com" "com.example.com.net")
        }
        assertFailsWith(BadParameterValue::class) {
            cmd("com.example.com.io" "com.example.com")
        }
    }

    @Test
    fun `to value is not as from value should complete normally`() {
        cmd("com.example.com" "com.example.app")
        assertEquals(cli.messages, emptyList())
    }

    private fun cmd(command: String) = cli.parse(command.split(' ').toTypedArray())

    companion object {
        private lateinit var cli: Chpkg

        @BeforeClass
        @JvmStatic
        fun initChpkg() {
            cli = Chpkg()
        }
    }
}
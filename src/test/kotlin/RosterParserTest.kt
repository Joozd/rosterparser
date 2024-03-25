import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.CSVParser
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RosterParserTest {
    private val csvTestFile = File(this::class.java.classLoader.getResource("joozdlogv5test.csv")!!.toURI())
    private val mimeType = "text/csv"

    @Test
    fun testParserCreation() {
        val parser = csvTestFile.inputStream().use {
            RosterParser.ofInputStream(it, mimeType)
        }
        assert(parser is RosterParser)
    }

    @Test
    fun testExceptIfUnableToCreateParser() {
        val emptyInputStream = "".byteInputStream()
        // check fail on bad data
        assertFailsWith<IllegalArgumentException> { RosterParser.ofInputStream(emptyInputStream, mimeType) }

        // check fail on bad mimetype
        assertFailsWith<IllegalArgumentException> { csvTestFile.inputStream().use { RosterParser.ofInputStream(it, "text/invalid") } }
    }
}
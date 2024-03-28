import nl.joozd.rosterparser.ParsedRoster
import nl.joozd.rosterparser.RosterParser
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class RosterParserTest {
    private val csvTestFile = File(this::class.java.classLoader.getResource("joozdlogv5test.csv")!!.toURI())
    private val mimeType = "text/csv"

    @Test
    fun testParserCreation() {
        val parser = csvTestFile.inputStream().use {
            RosterParser.ofInputStream(it, mimeType)
        }
        assertIs<RosterParser>(parser)
    }

    @Test
    fun `test if a ParsedRoster object is created`(){
        val parsedRoster = csvTestFile.inputStream().use {
            RosterParser.getRoster(it, mimeType)
        }
        assertIs<ParsedRoster>(parsedRoster)
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
package nl.joozd.rosterparser.testing
import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.ParsedRoster

/**
 * Test data to be passed to [RosterParser] to create a sample [ParsedRoster]
 * @see buildSampleRoster to see the type of roster provided
 */
object RosterParserTestData {
    const val mimeType = "text/plain"
    const val dataString = "RosterParser Test Data File. This data can be used to generate a test Roster with RosterParser."
    val inputstream get() = dataString.toByteArray().inputStream()
}
package nl.joozd.rosterparser.testing

object RosterParserTestData {
    const val mimeType = "text/plain"
    const val dataString = "RosterParser Test Data File. This data can be used to generate a test Roster with RosterParser."
    val inputstream get() = dataString.toByteArray().inputStream()
}
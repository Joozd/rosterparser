package nl.joozd.rosterparser.testing

import nl.joozd.rosterparser.MimeTypes

/**
 * Pasing this data into RosterParser will result in getting a sample roster
 * The sample roster will be as created by [buildSampleRoster]
 */
object RosterParserSampleFile {
    val EXPECTED_NUMBER_OF_FLIGHTS = SampleDuties.flightDuties.size
    val EXPECTED_NUMBER_OF_SIMS = SampleDuties.simDuties.size

    fun inputStream() = "RosterParser Test Data File. This data can be used to generate a test Roster with RosterParser.".toByteArray(Charsets.UTF_8).inputStream()
    val mimeType = MimeTypes.TEXT
}
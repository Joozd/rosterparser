package parsers.text

import nl.joozd.rosterparser.AirportFormat
import nl.joozd.rosterparser.parsers.factories.TextParserConstructor
import nl.joozd.rosterparser.parsers.text.TestDataParser
import nl.joozd.rosterparser.testing.SampleDuties
import org.junit.jupiter.api.Assertions.assertNotNull
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class TestDataParserTest: TextParserSubclassTest() {
    override val testResourceName: String = "sampledata.txt"
    override val parserConstructor: TextParserConstructor = TestDataParser
    override val expectedParserType = TestDataParser::class.java

    @Test
    fun testLogtenProOldParser() {
        assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<TestDataParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        val expectedFlights = SampleDuties.flightDuties
        val expectedSims = SampleDuties.simDuties

        assertEquals(
            expectedFlights.size + expectedSims.size,
            parsedRoster.parsedDuties.size,
            "Expected ${expectedFlights.size + expectedSims.size} duties in the parsed roster"
        ) // , got: ${parsedRoster.parsedDuties.joinToString("\n")}")
        assertEquals(ZoneOffset.UTC, parsedRoster.timezoneOfRoster, "Timezone of roster does not match the expected")
        assertFalse(parsedRoster.flightsArePlanned, "Expected flightsArePlanned to be false")


        assertEquals(expectedFlights.size, parsedRoster.flights.size, "Expected 3346 flights in the parsed roster")
        assertEquals(expectedFlights, parsedRoster.flights, "Parsed flights do not match the expected")

        assertEquals(expectedSims.size, parsedRoster.simulatorDuties.size, "Expected 66 simulator duty in the parsed roster")
        assertEquals(expectedSims, parsedRoster.simulatorDuties, "Parsed simulator duty does not match the expected")


//        assertEquals(correctTimeRange, parsedRoster.coveredDates, "Covered time range does not match the expected")
        assertEquals(AirportFormat.IATA, parsedRoster.airportFormat, "Airport Format does not match the expected")

        // TODO make test for balance forward.
    }
}
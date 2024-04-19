package parsers.text

import nl.joozd.rosterparser.parsers.factories.TextParserConstructor
import nl.joozd.rosterparser.parsers.text.LogtenProOldParser
import org.junit.jupiter.api.Assertions.assertNotNull
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class LogtenProOldParserTest: TextParserSubclassTest() {
    override val testResourceName: String = "logten_pro_old.txt"
    override val parserConstructor: TextParserConstructor = LogtenProOldParser

    @Test
    fun testJoozdlogV5Parser() {
        assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<LogtenProOldParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(3419, parsedRoster.parsedDuties.size, "Expected 3419 duties in the parsed roster") // , got: ${parsedRoster.parsedDuties.joinToString("\n")}")
        assertEquals(ZoneOffset.UTC, parsedRoster.timezoneOfRoster, "Timezone of roster does not match the expected")
        assertFalse(parsedRoster.flightsArePlanned, "Expected flightsArePlanned to be false")


        assertEquals(3346, parsedRoster.flights.size, "Expected 3346 flights in the parsed roster")
//        assertEquals(correctParsedFlight, parsedRoster.flights.first(), "Parsed flight does not match the expected")

        assertEquals(69, parsedRoster.simulatorDuties.size, "Expected 69 simulator duty in the parsed roster")
//        assertEquals(correctSimulatorDuty, parsedRoster.simulatorDuties.first(), "Parsed simulator duty does not match the expected")



        assertEquals(correctTimeRange, parsedRoster.coveredDates, "Covered time range does not match the expected")
//        assertEquals(correctAirportFormat, parsedRoster.airportFormat, "Airport Format does not match the expected")

        // TODO Make proper tests for correct data. Check at least a flight, balance forward and sim duty.
    }

    companion object{
        val correctTimeRange: ClosedRange<LocalDate> = LocalDate.of(2006, 9, 12)..LocalDate.of(2017, 9, 15)
    }
}
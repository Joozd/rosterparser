package parsers.csv

import nl.joozd.rosterparser.AirportFormat
import nl.joozd.rosterparser.parsers.csv.MccPilotLogParser
import nl.joozd.rosterparser.parsers.factories.CSVParserConstructor
import org.junit.jupiter.api.Assertions
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class MccPilotLogParserTest: CsvParserSubclassTest() {
    override val testResourceName: String = "mccpilotlogtest.csv"
    override val parserConstructor: CSVParserConstructor = MccPilotLogParser

    @Test
    fun testMccPilotLogParser() {
        Assertions.assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<MccPilotLogParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(2, parsedRoster.flights.size, "Expected 2 flight in the parsed roster")
        println(parsedRoster.flights.firstOrNull())

        assertEquals(13, parsedRoster.simulatorDuties.size, "Expected 13 simulator duties in the parsed roster")
        println(parsedRoster.simulatorDuties.firstOrNull())

        assertEquals(15, parsedRoster.parsedDuties.size, "Expected 15 duties in the parsed roster")
        assertEquals(ZoneOffset.UTC, parsedRoster.timezoneOfRoster, "Timezone of roster does not match the expected")
        assertFalse(parsedRoster.flightsArePlanned, "Expected flightsArePlanned to be false")
        println(parsedRoster.coveredDates)
        //assertEquals(JoozdlogV5ParserTest.correctTimeRange, parsedRoster.coveredDates, "Covered time range does not match the expected")
        assertEquals(correctAirportFormat, parsedRoster.airportFormat, "Airport Format does not match the expected")
    }


    companion object{
        val correctAirportFormat = AirportFormat.IATA
    }

}
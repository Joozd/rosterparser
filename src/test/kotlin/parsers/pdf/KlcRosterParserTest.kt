package parsers.pdf

import nl.joozd.rosterparser.AirportFormat
import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import nl.joozd.rosterparser.parsers.pdf.KlcRosterParser
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.*

class KlcRosterParserTest : PdfParserSubclassTest() {
    override val testResourceName: String = "klc_roster.pdf" // 61 flights!
    override val parserConstructor: PDFParserConstructor = KlcRosterParser


    @Test
    fun testParser() {
        assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<KlcRosterParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(10, parsedRoster.flights.size, "Expected 10 flights in the parsed roster, got \n${parsedRoster.flights.joinToString("\n")}")
//
        assertEquals(
            correctParsedFlight0.toString(),
            parsedRoster.flights.first().toString(),
            "Parsed flight 0 does not match the expected"
        )
        assertEquals(
            correctParsedFlight9.toString(),
            parsedRoster.flights.last().toString(),
            "Parsed flight 9 does not match the expected"
        )

        assertEquals(0, parsedRoster.simulatorDuties.size, "Expected 0 simulator duties in the parsed roster")
//        assertEquals(correctSimulatorDuty, parsedRoster.simulatorDuties.first(), "Parsed simulator duty does not match the expected")


        assertEquals(10, parsedRoster.parsedDuties.size, "Expected 2 duties in the parsed roster")
        assertEquals(ZoneOffset.UTC, parsedRoster.timezoneOfRoster, "Timezone of roster does not match the expected")
        assertTrue(
            parsedRoster.flightsArePlanned,
            "flightsArePlanned is not set as expected (actual value: ${parsedRoster.flightsArePlanned}"
        )
        assertEquals(correctTimeRange, parsedRoster.coveredDates, "Covered time range does not match the expected")
        assertEquals(correctAirportFormat, parsedRoster.airportFormat, "Airport Format does not match the expected")
    }

    companion object {
        private val correctParsedFlight0 =
            ParsedFlight(
                date = LocalDate.parse("2021-03-22"),
                flightNumber = "DH/KL1824",
                takeoffAirport = "BER",
                landingAirport = "AMS",
                departureTime = LocalDateTime.parse("2021-03-22T10:55"),
                arrivalTime = LocalDateTime.parse("2021-03-22T12:20"),
                aircraftType = null,
                isDeadHeading = true
            )

        private val correctParsedFlight9 =
            ParsedFlight(
                date = LocalDate.parse("2021-04-18"), // checks second month
                flightNumber = "KL1435",
                takeoffAirport = "AMS",
                landingAirport = "BHX",
                departureTime = LocalDateTime.parse("2021-04-18T19:05"),
                arrivalTime = LocalDateTime.parse("2021-04-18T20:20"),
                aircraftType = "E190",
                isDeadHeading = false
            )

//        private val correctSimulatorDuty = ParsedSimulatorDuty(
//            date = LocalDate.parse("2024-01-13"),
//            duration = 3.hours + 30.minutes,
//            simulatorType = "", // its actually B772 but the overview doesn't know that
//            remarks = "OPC/LPC"
//        )

        private val correctTimeRange = LocalDate.of(2021, 3, 22)..LocalDate.of(2021, 4, 18)
        val correctAirportFormat = AirportFormat.IATA
    }
}
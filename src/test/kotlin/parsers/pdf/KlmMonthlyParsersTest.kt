package parsers.pdf

import nl.joozd.rosterparser.AirportFormat
import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedSimulatorDuty
import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import nl.joozd.rosterparser.parsers.pdf.KlmMonthlyParser
import org.junit.jupiter.api.Assertions
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class KlmMonthlyParsersTest : PdfParserSubclassTest() {
    override val testResourceName: String = "klm_monthly.pdf"
    override val parserConstructor: PDFParserConstructor = KlmMonthlyParser
    override val expectedParserType = KlmMonthlyParser::class.java


    @Test
    fun testParser() {
        Assertions.assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<KlmMonthlyParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(6, parsedRoster.flights.size, "Expected 6 flights in the parsed roster")
//
        assertEquals(correctParsedFlight.toString(), parsedRoster.flights.first().toString(), "Parsed flight does not match the expected")

        assertEquals(1, parsedRoster.simulatorDuties.size, "Expected 1 simulator duty in the parsed roster")
        assertEquals(correctSimulatorDuty, parsedRoster.simulatorDuties.first(), "Parsed simulator duty does not match the expected")


        assertEquals(7, parsedRoster.parsedDuties.size, "Expected 2 duties in the parsed roster")
        assertEquals(ZoneOffset.UTC, parsedRoster.timezoneOfRoster, "Timezone of roster does not match the expected")
        assertFalse(parsedRoster.flightsArePlanned, "flightsArePlanned is not set as expected (actual value: ${parsedRoster.flightsArePlanned}")
        assertEquals(correctTimeRange, parsedRoster.coveredDates, "Covered time range does not match the expected")
        assertEquals(correctAirportFormat, parsedRoster.airportFormat, "Airport Format does not match the expected")
    }

    companion object {
        private val correctParsedFlight =
            ParsedFlight(
                date = LocalDate.parse("2024-01-04"),
                flightNumber = "KL643", takeoffAirport = "AMS",
                landingAirport = "JFK",
                departureTime = LocalDateTime.parse("2024-01-04T15:18"),
                arrivalTime = LocalDateTime.parse("2024-01-05T05:33"),
                aircraftRegistration = "PHBHO",
                isPICDuty = false
            )

        private val correctSimulatorDuty = ParsedSimulatorDuty(
            date = LocalDate.parse("2024-01-13"),
            duration = 3.hours + 30.minutes,
            simulatorType = "", // its actually B772 but the overview doesn't know that
            remarks = "OPC/LPC"
        )

        private val correctTimeRange = LocalDate.of(2024, 1, 1)..LocalDate.of(2024, 1, 31)
        val correctAirportFormat = AirportFormat.IATA
    }
}
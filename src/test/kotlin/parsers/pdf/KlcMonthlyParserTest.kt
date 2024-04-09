package parsers.pdf

import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import nl.joozd.rosterparser.parsers.pdf.KlcMonthlyParser
import org.junit.jupiter.api.Assertions
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class KlcMonthlyParsersTest: PdfParserSubclassTest() {
    override val testResourceName: String = "klc_monthly.pdf" // 61 flights!
    override val parserConstructor: PDFParserConstructor = KlcMonthlyParser


    @Test
    fun testParser() {
        Assertions.assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<KlcMonthlyParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(61, parsedRoster.flights.size, "Expected 6 flights in the parsed roster")
//
        assertEquals(correctParsedFlight.toString(), parsedRoster.flights.first().toString(), "Parsed flight does not match the expected")

        assertEquals(0, parsedRoster.simulatorDuties.size, "Expected 0 simulator duties in the parsed roster")
//        assertEquals(correctSimulatorDuty, parsedRoster.simulatorDuties.first(), "Parsed simulator duty does not match the expected")


        assertEquals(61, parsedRoster.parsedDuties.size, "Expected 2 duties in the parsed roster")
        assertEquals(ZoneOffset.UTC, parsedRoster.timezoneOfRoster, "Timezone of roster does not match the expected")
        assertFalse(parsedRoster.flightsArePlanned, "flightsArePlanned is not set as expected (actual value: ${parsedRoster.flightsArePlanned}")
        assertEquals(correctTimeRange, parsedRoster.coveredDates, "Covered time range does not match the expected")
    }

    companion object{
        private val correctParsedFlight =
            ParsedFlight(
                date = LocalDate.parse("2018-05-01"),
                flightNumber = "KL1327",
                takeoffAirport = "AMS",
                landingAirport = "AES",
                departureTime = LocalDateTime.parse("2018-05-01T19:10"),
                arrivalTime = LocalDateTime.parse("2018-05-01T21:00"),
                aircraftRegistration = "PH-EXG"
            )

//        private val correctSimulatorDuty = ParsedSimulatorDuty(
//            date = LocalDate.parse("2024-01-13"),
//            duration = 3.hours + 30.minutes,
//            simulatorType = "", // its actually B772 but the overview doesn't know that
//            remarks = "OPC/LPC"
//        )

        private val correctTimeRange = LocalDate.of(2018,5,1)..LocalDate.of(2018, 5, 31)
    }
}
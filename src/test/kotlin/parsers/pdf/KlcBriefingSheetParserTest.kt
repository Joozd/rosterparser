package parsers.pdf

import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.Person
import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import nl.joozd.rosterparser.parsers.pdf.KlcBriefingSheetParser
import org.junit.jupiter.api.Assertions
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.*

class KlcBriefingSheetParserTest: PdfParserSubclassTest() {
    override val testResourceName: String = "klcbriefingsheettest.pdf"
    override val parserConstructor: PDFParserConstructor = KlcBriefingSheetParser


    @Test
    fun testParser() {
        Assertions.assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<KlcBriefingSheetParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(6, parsedRoster.flights.size, "Expected 6 flights in the parsed roster")
//
        assertEquals(correctParsedFlight.toString(), parsedRoster.flights.first().toString(), "Parsed flight does not match the expected")


        //assertEquals(2, parsedRoster.parsedDuties.size, "Expected 2 duties in the parsed roster")
        assertEquals(ZoneOffset.UTC, parsedRoster.timezoneOfRoster, "Timezone of roster does not match the expected")
        assertTrue(parsedRoster.flightsArePlanned, "Expected flightsArePlanned to be false")
        assertEquals(correctTimeRange, parsedRoster.coveredDates, "Covered time range does not match the expected")
    }

    companion object{
        private val correctParsedFlight =
            ParsedFlight(date = LocalDate.parse("2024-01-25"),
                flightNumber = "KL1857", takeoffAirport = "AMS",
                landingAirport = "DUS",
                departureTime = LocalDateTime.parse("2024-01-25T12:10"),
                arrivalTime = LocalDateTime.parse("2024-01-25T12:55"),
                aircraftRegistration = "PHEZV",
                pilotInCommand = Person.fromString("Joost Welle"),
                personsNotPIC = listOf(Person.fromString("Joery Folkers"), Person.fromString("Romario Ter Horst"), Person.fromString("Patri­ van der Wolk")),
                isPICDuty = true
            )

        private val correctTimeRange = LocalDate.of(2024,1,25)..LocalDate.of(2024, 1, 27)
    }
}
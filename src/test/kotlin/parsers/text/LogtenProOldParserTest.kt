package parsers.text

import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedSimulatorDuty
import nl.joozd.rosterparser.Person
import nl.joozd.rosterparser.parsers.factories.TextParserConstructor
import nl.joozd.rosterparser.parsers.text.LogtenProOldParser
import org.junit.jupiter.api.Assertions.assertNotNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class LogtenProOldParserTest: TextParserSubclassTest() {
    override val testResourceName: String = "logten_pro_old.txt"
    override val parserConstructor: TextParserConstructor = LogtenProOldParser
    override val expectedParserType = LogtenProOldParser::class.java

    @Test
    fun testLogtenProOldParser() {
        assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<LogtenProOldParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(3419, parsedRoster.parsedDuties.size, "Expected 3419 duties in the parsed roster") // , got: ${parsedRoster.parsedDuties.joinToString("\n")}")
        assertEquals(ZoneOffset.UTC, parsedRoster.timezoneOfRoster, "Timezone of roster does not match the expected")
        assertFalse(parsedRoster.flightsArePlanned, "Expected flightsArePlanned to be false")


        assertEquals(3346, parsedRoster.flights.size, "Expected 3346 flights in the parsed roster")
        assertEquals(correctParsedFlight, parsedRoster.flights[5], "Parsed flight does not match the expected")

        assertEquals(66, parsedRoster.simulatorDuties.size, "Expected 66 simulator duty in the parsed roster")
        assertEquals(correctSimulatorDuty.toString(), parsedRoster.simulatorDuties[0].toString(), "Parsed simulator duty does not match the expected")



        assertEquals(correctTimeRange, parsedRoster.coveredDates, "Covered time range does not match the expected")
//        assertEquals(correctAirportFormat, parsedRoster.airportFormat, "Airport Format does not match the expected")

        // TODO make test for balance forward.
    }

    companion object{
        private val correctTimeRange: ClosedRange<LocalDate> = LocalDate.of(2006, 9, 12)..LocalDate.of(2017, 9, 15)

        // Not all fields tested, like PIC stuff. Could do with another flight to test that.
        private val correctParsedFlight = ParsedFlight(
            date = LocalDate.parse("2007-06-15"),
            flightNumber = "KL753",
            takeoffAirport = "AMS",
            landingAirport = "BON",
            departureTime = LocalDateTime.parse("2007-06-15T22:04"),
            arrivalTime = LocalDateTime.parse("2007-06-16T07:20"),  // Assuming next day arrival for sample data
            aircraftType = "MD11",
            aircraftRegistration = "PHKCG",
            overriddenTotalTime = 9.hours + 16.minutes,
            ifrTime = 9.hours + 16.minutes,
            nightTime = 9.hours + 16.minutes,
            multiPilotTime = 9.hours + 16.minutes,
            pilotInCommand = Person.fromString("Sier, Bart"),
            numberOfAutolands = 0,
            personsNotPIC = listOf(Person.fromString("Self")),
            isPF=false,
            isPICDuty=false,
            isPICUSDuty=false,
            isCopilotDuty=true,
            isDualDuty = false
        )

        private val correctSimulatorDuty = ParsedSimulatorDuty(
            date = LocalDate.parse("2007-04-19"),
            duration = 3.hours + 30.minutes,
            simulatorType = "MD11",
            remarks = "TQ 1",
            persons = listOf(Person("Self"))
        )
    }
}
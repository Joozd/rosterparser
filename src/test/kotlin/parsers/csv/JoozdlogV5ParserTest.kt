package parsers.csv

import nl.joozd.rosterparser.AirportFormat
import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedSimulatorDuty
import nl.joozd.rosterparser.Person
import nl.joozd.rosterparser.parsers.csv.JoozdlogV5Parser
import nl.joozd.rosterparser.parsers.factories.CSVParserConstructor
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

class JoozdlogV5ParserTest: CsvParserSubclassTest() {
    override val testResourceName: String = "joozdlogv5test.csv"
    override val parserConstructor: CSVParserConstructor = JoozdlogV5Parser

    @Test
    fun testJoozdlogV5Parser() {
        assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<JoozdlogV5Parser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(1, parsedRoster.flights.size, "Expected 1 flight in the parsed roster")
        assertEquals(correctParsedFlight, parsedRoster.flights.first(), "Parsed flight does not match the expected")

        assertEquals(1, parsedRoster.simulatorDuties.size, "Expected 1 simulator duty in the parsed roster")
        assertEquals(correctSimulatorDuty, parsedRoster.simulatorDuties.first(), "Parsed simulator duty does not match the expected")

        assertEquals(2, parsedRoster.parsedDuties.size, "Expected 2 duties in the parsed roster")
        assertEquals(ZoneOffset.UTC, parsedRoster.timezoneOfRoster, "Timezone of roster does not match the expected")
        assertFalse(parsedRoster.flightsArePlanned, "Expected flightsArePlanned to be false")
        assertEquals(correctTimeRange, parsedRoster.coveredDates, "Covered time range does not match the expected")
        assertEquals(correctAirportFormat, parsedRoster.airportFormat, "Airport Format does not match the expected")
    }

    companion object{
        private val correctParsedFlight = ParsedFlight(
            date = LocalDate.parse("2024-03-05"),
            flightNumber = "KL807",
            takeoffAirport = "EHAM",
            landingAirport = "RCTP",
            departureTime = LocalDateTime.parse("2024-03-05T19:20"),
            arrivalTime = LocalDateTime.parse("2024-03-06T07:54"),
            overriddenTotalTime = null,
            multiPilotTime = 7.hours + 2.minutes,
            nightTime = 3.hours + 12.minutes,
            ifrTime = 7.hours + 2.minutes,
            aircraftType = "B789",
            aircraftRegistration = "PH-BHO",
            numberOfTakeoffsByDay = 0,
            numberOfTakeoffsByNight = 1,
            numberOfLandingsByDay = 1,
            numberOfLandingsByNight = 0,
            numberOfAutolands = 0,
            crewSize = 4,
            atControlsForTakeoff = true,
            atControlsForLanding = true,
            augmentedCrewTimeForTakeoffLanding = 45.minutes,
            augmentedCrewFixedRestTime = null,
            pilotInCommand = Person.fromString("PIC Name"),
            personsNotPIC = listOf("other name 1", "SELF", "Yet Another Name").map { Person.fromString(it)},
            isPF = true,
            isPICDuty = false,
            isPICUSDuty = false,
            isCopilotDuty = true,
            isInstructorDuty = false,
            isDualDuty = false,
            remarks = "",
            signatureSVG = ""
        )

        private val correctSimulatorDuty = ParsedSimulatorDuty(
            date = LocalDate.parse("2024-01-13"),
            duration = 3.hours + 30.minutes,
            simulatorType = "B772",
            remarks = "OPC/LPC",
            persons = listOf("Instructors name", "Other Trainees Name").map { Person.fromString(it)},
            instructionGiven = false
        )

        val correctTimeRange: ClosedRange<LocalDate> = LocalDate.of(2024, 1, 13)..LocalDate.of(2024, 3, 5)

        val correctAirportFormat = AirportFormat.ICAO

    }
}
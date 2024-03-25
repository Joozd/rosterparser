package parsers.csv

import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedSimulatorDuty
import nl.joozd.rosterparser.ParsingException
import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.csv.JoozdlogV5Parser
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class JoozdlogV5ParserTest {
    private val joozdLogV5TestFile = File(this::class.java.classLoader.getResource("joozdlogv5test.csv")!!.toURI())

    @Test
    fun testJoozdlogV5Parser(){
        val lines = joozdLogV5TestFile.readLines()
        val parser = JoozdlogV5Parser.createIfAble(lines)
        //check if parser can be made from the data and is of the correct type
        assert(parser != null)
        assert(parser is JoozdlogV5Parser)
        assert(parser is RosterParser)

        val parsedRoster = parser!!.getRoster()

        assert(parsedRoster.flights.size == 1)
        assertEquals(correctParsedFlight, parsedRoster.flights.first())

        assert(parsedRoster.simulatorDuties.size == 1)
        assertEquals(correctSimulatorDuty, parsedRoster.simulatorDuties.first())

        assert(parsedRoster.parsedDuties.size == 2)
        assert(parsedRoster.timezoneOfRoster == ZoneOffset.UTC)
        assert(!parsedRoster.flightsArePlanned)
        assertEquals(correctTimeRange, parsedRoster.coveredTime)

        // check bad data handling:
        assertNull(JoozdlogV5Parser.createIfAble(listOf("Bad Data")))
        assertFailsWith<ParsingException> { JoozdlogV5Parser(listOf("Bad Data", "even more bad data")).getRoster() }
    }

    companion object{
        private val correctParsedFlight = ParsedFlight(
            date = LocalDate.parse("2024-03-05"),
            flightNumber = "KL807",
            takeoffAirport = "EHAM",
            landingAirport = "RCTP",
            departureTime = LocalDateTime.parse("2024-03-05T19:20"),
            arrivalTime = LocalDateTime.parse("2024-03-05T19:20"),
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
            crewSize = 4,
            atControlsForTakeoff = true,
            atControlsForLanding = true,
            augmentedCrewTimeForTakeoffLanding = 45.minutes,
            augmentedCrewFixedRestTime = null,
            namePIC = "PIC Name",
            namesNotPIC = listOf("other name 1", "SELF", "Yet Another Name"),
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
            names = listOf("Instructors name", "Other Trainees Name"),
            instructionGiven = false
        )

        val correctTimeRange: ClosedRange<LocalDateTime> = LocalDateTime.of(2024, 1, 13, 0, 0)..LocalDateTime.of(2024, 3, 6, 0, 0)

    }
}
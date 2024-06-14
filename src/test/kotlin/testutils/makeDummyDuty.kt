package testutils

import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedSimulatorDuty
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.minutes

internal fun makeDummyFlightDuty(date: LocalDate): ParsedFlight = ParsedFlight(
    date = date,
    arrivalTime = LocalDateTime.MIN,
    departureTime = LocalDateTime.MIN,
    takeoffAirport = "null",
    landingAirport = "null"
)

internal fun makeDummySimDuty(date: LocalDate): ParsedSimulatorDuty = ParsedSimulatorDuty(
    date = date,
    duration = 1.minutes
)

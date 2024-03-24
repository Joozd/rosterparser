package nl.joozd.rosterparser

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * A ParsedFlight is a flight found in a roster. These should be kept in a [ParsedRoster] for accompanying metadata.
 *
 * @property flightNumber The flight number of this flight. Can be empty ("") for flights that don't have one.
 * @property takeoffAirport Airport of departure. can be any format, e.g. AMS, EHAM, or Amsterdam Schiphol Airport, depending on the provided roster.
 * @property landingAirport Airport of arrival. Can be any format, e.g. AMS, EHAM, or Amsterdam Schiphol Airport, depending on the provided roster.
 * @property date Date of departure. Can be in any timezone, depending on the provided roster. Timezone should be provided in [ParsedRoster].
 * @property departureTime Moment of departure. Can be in any timezone, depending on the provided roster. Timezone should be provided in [ParsedRoster].
 * @property namePIC name of PIC. Optional, defaults to null.
 * @property namesNotPIC names of crew members that are not PIC. Optional, defaults to null.
 */
data class ParsedFlight(
    val flightNumber: String = "",
    val takeoffAirport: String,
    val landingAirport: String,
    val date: LocalDate,
    val departureTime: LocalDateTime,
    val arrivalTime: LocalDateTime,
    val namePIC: String? = null,
    val namesNotPIC: List<String>? = null
): ParsedDuty
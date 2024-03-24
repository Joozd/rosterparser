package nl.joozd.rosterparser

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Duration

/**
 * Represents a flight found within a roster. For organization and context, instances of this class should be contained within a [ParsedRoster].
 *
 * @property flightNumber The flight number. It can be an empty string ("") for flights without a number.
 * @property takeoffAirport The departure airport. Format varies (e.g., AMS, EHAM, Amsterdam Schiphol Airport) based on the roster provided.
 * @property landingAirport The arrival airport. Format can vary similar to takeoffAirport, based on the roster provided.
 * @property date The departure date. The timezone can vary and is specified in the accompanying [ParsedRoster].
 * @property departureTime The scheduled departure time. Timezone specifics are as per the roster and noted in [ParsedRoster].
 * @property arrivalTime The scheduled arrival time. Timezone treatment is similar to departureTime and date.
 *
 * @property overriddenTotalTime The total duration of the flight if it differs from the calculated duration between [departureTime] and [arrivalTime]. Optional, defaults to null.
 * @property nightTime The duration of the flight that occurred during nighttime. Optional, defaults to null.
 * @property ifrTime The duration of the flight operated under Instrument Flight Rules (IFR). Optional, defaults to null.
 * @property aircraftType The type of aircraft used for the flight. Optional, defaults to null.
 * @property aircraftRegistration The registration code of the aircraft used. Optional, defaults to null.
 * @property numberOfTakeoffsByDay The number of takeoffs performed during daylight. Optional, defaults to null.
 * @property numberOfTakeoffsByNight The number of takeoffs performed during nighttime. Optional, defaults to null.
 * @property numberOfLandingsByDay The number of landings performed during daylight. Optional, defaults to null.
 * @property numberOfLandingsByNight The number of landings performed during nighttime. Optional, defaults to null.
 *
 * @property namePIC The name of the Pilot in Command (PIC). Optional, defaults to null.
 * @property namesNotPIC The names of crew members who are not the PIC. Optional, defaults to null.
 * @property isPICDuty Indicates if the duty was performed as PIC. Optional, defaults to null.
 * @property isPICUSDuty Indicates if the duty was performed under PICUS (Pilot in Command Under Supervision) conditions. Optional, defaults to null.
 * @property isCopilotDuty Indicates if the duty was performed as a co-pilot. Optional, defaults to null.
 * @property isInstructorDuty Indicates if the duty involved acting as an instructor. Optional, defaults to null.
 * @property isDualDuty Indicates if the duty was performed as dual instruction, meaning both receiving and providing instruction. Optional, defaults to null.
 * @property remarks Any additional remarks related to the flight, akin to the "Remarks" field in a pilot's logbook. Optional, defaults to null.
 * @property signatureSVG A digital representation of the signature, typically in SVG format. Optional, defaults to null.
 */

data class ParsedFlight(
    val flightNumber: String = "",
    val takeoffAirport: String,
    val landingAirport: String,
    val date: LocalDate,
    val departureTime: LocalDateTime,
    val arrivalTime: LocalDateTime,

    val overriddenTotalTime: Duration? = null,
    val nightTime: Duration? = null,
    val ifrTime: Duration? = null,
    val aircraftType: String? = null,
    val aircraftRegistration: String? = null,
    val numberOfTakeoffsByDay: Int? = null,
    val numberOfTakeoffsByNight: Int? = null,
    val numberOfLandingsByDay: Int? = null,
    val numberOfLandingsByNight: Int? = null,

    val namePIC: String? = null,
    val namesNotPIC: List<String>? = null,
    val isPICDuty: Boolean? = null,
    val isPICUSDuty: Boolean? = null,
    val isCopilotDuty: Boolean? = null,
    val isInstructorDuty: Boolean? = null,
    val isDualDuty: Boolean? = null,
    val remarks: String? = null,
    val signatureSVG: String? = null
): ParsedDuty
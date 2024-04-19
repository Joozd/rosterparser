package nl.joozd.rosterparser

import java.time.LocalDate
import kotlin.time.Duration

/**
 * Represents a Balance Forward entry in a logbook, typically used to carry forward totals from previous log entries.
 * This class aggregates previous flying experiences to be included in new log calculations.
 *
 * @property date The date of this Balance Forward entry, as a LocalDate. Can be in any timezone, depending on the provided roster.
 * @property totalTime The total time logged in this balance forward. Optional, defaults to null.
 * @property picTime The time flown as Pilot in Command (PIC). Optional, defaults to null.
 * @property multiPilotTime The duration of the flight that occurred during Multi Pilot operations. Optional, defaults to null.
 * @property nightTime The duration of the flight that occurred during nighttime. Optional, defaults to null.
 * @property xcTime The duration of the flight operated under Cross Country conditions. Optional, defaults to null.
 * @property ifrTime The duration of the flight operated under Instrument Flight Rules (IFR). Optional, defaults to null.
 * @property actualIfrTime The actual time flown under Instrument Flight Rules (IFR), distinct from [ifrTime] which should include this time if not null. Optional, defaults to null.
 * @property dualReceivedTime The duration of the flight where Dual instruction is received. Optional, defaults to null.
 * @property dualGivenTime The duration of the flight where Dual instruction is given. Optional, defaults to null.
 * @property simulatorTime The time spent in a flight simulator, contributing to training or proficiency requirements. Optional, defaults to null.
 * @property numberOfTakeoffsByDay The number of takeoffs performed during daylight. Optional, defaults to null.
 * @property numberOfTakeoffsByNight The number of takeoffs performed during nighttime. Optional, defaults to null.
 * @property numberOfLandingsByDay The number of landings performed during daylight. Optional, defaults to null.
 * @property numberOfLandingsByNight The number of landings performed during nighttime. Optional, defaults to null.
 * @property numberOfAutolands The number of automatic landings performed. This is typically used for certain types of aircraft or under specific operating conditions. Optional, defaults to null.
 * @property aircraftType The type of aircraft used for the flights covered by this Balance Forward. Optional, defaults to null.
 * @property remarks Any additional remarks related to the accumulated experiences or specifics not captured elsewhere in the record. Optional, defaults to null.
 */
data class BalanceForward(
    override val date: LocalDate,
    val totalTime: Duration? = null,
    val picTime: Duration? = null,
    val multiPilotTime: Duration? = null,
    val nightTime: Duration? = null,
    val xcTime: Duration? = null,
    val ifrTime: Duration? = null,
    val actualIfrTime: Duration? = null,
    val dualReceivedTime: Duration? = null,
    val dualGivenTime: Duration? = null,
    val simulatorTime: Duration? = null,
    val numberOfTakeoffsByDay: Int? = null,
    val numberOfTakeoffsByNight: Int? = null,
    val numberOfLandingsByDay: Int? = null,
    val numberOfLandingsByNight: Int? = null,
    val numberOfAutolands: Int? = null,
    val aircraftType: String? = null,
    val remarks: String? = null,
): ParsedDuty
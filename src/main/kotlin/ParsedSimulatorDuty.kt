package nl.joozd.rosterparser

import java.time.LocalDate
import kotlin.time.Duration

/**
 * A simulator duty
 * @property date The date of this session, as a LocalDate. Can be in any timezone, depending on the provided roster.
 * @property duration The Duration of this simulator duty.
 * @property simulatorType The type of simulator used (like B789, or FNPT-2)
 * @property remarks Remarks about this simulator duty, like the name of the session.
 * @property persons List of people in this simulator duty. Optional, defaults to null.
 * @property instructionGiven If true, this simulator duty was given as instructor. Optional, defaults to null.
 */
data class ParsedSimulatorDuty(
    override val date: LocalDate,
    val duration: Duration,
    val simulatorType: String,
    val remarks: String? = null,
    val persons: List<Person>? = null,
    val instructionGiven: Boolean? = null
): ParsedDuty

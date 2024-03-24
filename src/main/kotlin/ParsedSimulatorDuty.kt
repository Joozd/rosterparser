package nl.joozd.rosterparser

import nl.joozd.rosterparser.ParsedDuty
import java.time.LocalDate
import kotlin.time.Duration

/**
 * A simulator duty
 * @property date The date of this session, as a LocalDate. Can be in any timezone, depending on the provided roster.
 * @property duration The Duration of this simulator duty.
 * @property remarks Remarks about this simulator duty, like the name of the session.
 * @property names List of names of people in this simulator duty. Optional, defaults to null.
 * @property instructionGiven If true, this simulator duty was given as instructor. Optional, defaults to null.
 */
data class ParsedSimulatorDuty(
    val date: LocalDate,
    val duration: Duration,
    val remarks: String?,
    val names: List<String>? = null,
    val instructionGiven: Boolean? = null
): ParsedDuty

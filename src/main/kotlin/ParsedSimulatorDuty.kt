package nl.joozd.rosterparser

import nl.joozd.rosterparser.ParsedDuty
import kotlin.time.Duration

/**
 * A simulator duty
 * @property duration The duration of this simulator duty.
 */
data class ParsedSimulatorDuty(
    val duration: Duration
): ParsedDuty

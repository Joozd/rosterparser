package nl.joozd.rosterparser

import java.time.LocalDateTime
import java.time.ZoneId


/**
 * A Parsed roster contains the parsed duties, as well as metadata about the parsed roster.
 * @property parsedDuties A list of all the duties in this roster
 * @property timezoneOfRoster The timezone of all the times in this roster
 * @property coveredTime The time span covered by this roster (e.g. 2024-03-24 00:00 until 2024-03-31 00:00)
 */
data class ParsedRoster(
    val parsedDuties: List<ParsedDuty>,
    val timezoneOfRoster: ZoneId,
    val coveredTime: ClosedRange<LocalDateTime>
) {
    /**
     * Only the ParsedFlight items in [parsedDuties]
     */
    val flights: List<ParsedFlight> get() = parsedDuties.filterIsInstance<ParsedFlight>()

    /**
     * Only the ParsedSimulatorDuty items in [parsedDuties]
     */
    val simulatorDuties: List<ParsedSimulatorDuty> get() = parsedDuties.filterIsInstance<ParsedSimulatorDuty>()
}
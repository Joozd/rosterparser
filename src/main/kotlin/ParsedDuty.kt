package nl.joozd.rosterparser

import java.time.LocalDate

/**
 * A ParsedDuty is a duty found on a roster. Probably a [ParsedFlight] or a [ParsedSimulatorDuty]
 * @property date The departure date. The timezone can vary and is specified in the accompanying [ParsedRoster].
 */
interface ParsedDuty {
    val date: LocalDate
}
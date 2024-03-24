package nl.joozd.rosterparser

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset


/**
 * A Parsed roster contains the parsed duties, as well as metadata about the parsed roster.
 * @property parsedDuties A list of all the duties in this roster
 * @property timezoneOfRoster The timezone of all the times in this roster
 * @property coveredTime The time span covered by this roster (e.g. 2024-03-24 00:00 until 2024-03-31 00:00)
 * @property flightsArePlanned if true, flights are planned (a roster). If false, flights are completed (an overview).
 */
data class ParsedRoster(
    val parsedDuties: List<ParsedDuty>,
    val timezoneOfRoster: ZoneId,
    val coveredTime: ClosedRange<LocalDateTime>,
    val flightsArePlanned: Boolean = true
) {
    /**
     * Only the ParsedFlight items in [parsedDuties]
     */
    val flights: List<ParsedFlight> get() = parsedDuties.filterIsInstance<ParsedFlight>()

    /**
     * Only the ParsedSimulatorDuty items in [parsedDuties]
     */
    val simulatorDuties: List<ParsedSimulatorDuty> get() = parsedDuties.filterIsInstance<ParsedSimulatorDuty>()

    /**
     * An internal builder class for constructing instances of [ParsedRoster].
     * This class allows for the step-by-step construction of a [ParsedRoster] object,
     * enabling the specification of time zone, roster start and end times, etc. and the adding of duties to the roster.
     *
     * The builder starts with a default configuration of UTC time zone, a covered time
     * of [LocalDateTime.MIN]..[LocalDateTime.MAX] and no duties. These defaults can be overridden by calling the
     * appropriate methods on the builder.
     *
     * Duties can be added one by one, by calling [addDuty]
     *
     * @property timeZone The time zone of the roster, defaulting to [ZoneOffset.UTC].
     * @property rosterStart The start time of the roster period, defaulting to [LocalDateTime.MIN].
     * @property rosterEnd The end time of the roster period, defaulting to [LocalDateTime.MAX].
     * @property flightsArePlanned if true, flights are planned (a roster). If false, flights are completed (an overview).
     */
    internal class Builder {
        private val foundDuties = ArrayList<ParsedDuty>()
        var timeZone: ZoneId = ZoneOffset.UTC
        var rosterStart: LocalDateTime = LocalDateTime.MIN
        var rosterEnd: LocalDateTime = LocalDateTime.MAX
        var flightsArePlanned = true


        /**
         * Adds a [ParsedDuty] to the list of duties for the roster.
         *
         * @param duty The [ParsedDuty] to be added to the roster.
         */
        fun addDuty(duty: ParsedDuty) {
            foundDuties.add(duty)
        }

        /**
         * Constructs a [ParsedRoster] with the current configuration of the builder.
         * This method uses the duties added via [addDuty], and the time zone, start, and end
         * times specified through direct property access.
         *
         * @return A [ParsedRoster] instance configured according to the builder's settings.
         */
        fun build() = ParsedRoster(
            parsedDuties = foundDuties,
            timezoneOfRoster = timeZone,
            coveredTime = rosterStart..rosterEnd,
            flightsArePlanned = flightsArePlanned
        )
    }

    companion object {
        internal fun build(init: Builder.() -> Unit): ParsedRoster {
            // Create an instance of Builder, apply the lambda, and then build the ParsedRoster
            return Builder().apply(init).build()
        }
    }
}
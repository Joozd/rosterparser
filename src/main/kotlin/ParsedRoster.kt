package nl.joozd.rosterparser

import nl.joozd.rosterparser.ParsedRoster.Companion.build
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * A Parsed roster contains the parsed duties, as well as metadata about the parsed roster.
 * @property parsedDuties A list of all the duties in this roster
 * @property timezoneOfRoster The timezone of all the times in this roster
 * @property coveredDates The time span covered by this roster (e.g. 2024-03-24 until 2024-03-31)
 * @property flightsArePlanned if true, flights are planned (a roster). If false, flights are completed (an overview).
 */
data class ParsedRoster(
    val parsedDuties: List<ParsedDuty>,
    val timezoneOfRoster: ZoneId,
    val coveredDates: ClosedRange<LocalDate>,
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
     * A builder for constructing instances of [ParsedRoster], providing a fluent interface
     * for configuring the roster, including setting the timezone, adding duties, and defining
     * the roster period. The roster period defaults to spanning from the start of the first duty
     * to the end of the last duty, or is undefined if no duties are added.
     *
     * @property timeZone The time zone of the roster, defaulting to [ZoneOffset.UTC].
     * @property rosterPeriod The period of the roster. If not set, will default to the start of the first duty to the end of the last duty.
     * @property flightsArePlanned if true, flights are planned (a roster). If false, flights are completed (an overview).
     */
    internal class Builder {
        private val foundDuties = ArrayList<ParsedDuty>()
        var timeZone: ZoneId = ZoneOffset.UTC
        var rosterPeriod: ClosedRange<LocalDate>? = null
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
         * Calculates the period covered by this roster.
         * - returns [rosterPeriod] is it is set, otherwise calculates it from the added duties.
         * This function should be called during building of the [ParsedRoster] object.
         */

        private fun calculatePeriod(): ClosedRange<LocalDate> = rosterPeriod
            ?: calculatePeriodFromDuties()


        /**
         * Calculates the period of this duty by looking at the dates of the duties in [foundDuties].
         * - By default, spans from the start of the day of the first duty to the end of the day of the last duty.
         * - If no duties are added, defaults to an empty range from [LocalDate.MIN] to [LocalDate.MIN],
         *   indicating an undefined period.
         *
         * This calculation ensures an accurate reflection of the time span of the roster's duties and is
         * finalized upon invoking the [build] method.
         */
        private fun calculatePeriodFromDuties(): ClosedRange<LocalDate>{
            val earliestDate = foundDuties.minOfOrNull { it.date } ?: return LocalDate.MIN..LocalDate.MIN
            val latestDate = foundDuties.maxOfOrNull { it.date } ?: return LocalDate.MIN..LocalDate.MIN
            return earliestDate..latestDate
        }


        /**
         * Build the ParsedRoster from the data set in this Builder.
         */
        fun build() = ParsedRoster(
            parsedDuties = foundDuties,
            timezoneOfRoster = timeZone,
            coveredDates = calculatePeriod(),
            flightsArePlanned = flightsArePlanned
        )
    }

    companion object {
        /**
         * Builds a [ParsedRoster] instance using the [Builder] to configure its properties.
         * Within the [init] lambda, you have access to the [Builder]'s functions and properties
         * allowing you to set options such as [Builder.timeZone], [Builder.rosterPeriod],
         * and [Builder.flightsArePlanned]. You can also add flights using [Builder.addDuty].
         *
         * @param init A lambda with [Builder] as its receiver, used for configuring the [ParsedRoster].
         * @return A configured [ParsedRoster] instance.
         * @see Builder for more details on the available configuration options.
         */
        internal fun build(init: Builder.() -> Unit): ParsedRoster {
            // Create an instance of Builder, apply the lambda, and then build the ParsedRoster
            return Builder().apply(init).build()
        }
    }
}
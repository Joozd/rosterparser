package nl.joozd.rosterparser.testing

import nl.joozd.rosterparser.AirportFormat
import nl.joozd.rosterparser.ParsedRoster
import java.time.ZoneOffset

/**
 * Builds a sample roster with data from [SampleDuties]
 * @return in a [ParsedRoster] with the following data inside:
 *  Flight duties as in [SampleDuties.flightDuties]
 *  Sim duties as in [SampleDuties.simDuties]
 *  Timezone set to UTC
 *  Period set to the range of the duties covered in flight and sim duties.
 *  Flights are not planned.
 */
fun buildSampleRoster() = ParsedRoster.build{
    SampleDuties.flightDuties.forEach { addDuty(it)}
    SampleDuties.simDuties.forEach { addDuty(it)}

    airportFormat = AirportFormat.IATA

    timeZone = ZoneOffset.UTC

    flightsArePlanned = false
}
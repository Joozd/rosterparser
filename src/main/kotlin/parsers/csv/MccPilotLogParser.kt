package nl.joozd.rosterparser.parsers.csv

import nl.joozd.rosterparser.*
import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.factories.CSVParserConstructor
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Parses a MCC Pilot Log tab-separated CSV file
 * This is the format I have for my logbook from the 2010's.
 */
class MccPilotLogParser(private val lines: List<String>) : CSVParser() {
    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws ParsingException when the data used to construct this parser cannot be parsed after all
     */
    override fun getRoster(): ParsedRoster = ParsedRoster.build {
        flightsArePlanned = false
        // period is left at default (auto-set by Builder)

        val keys = lines.firstOrNull()?.split('\t') ?: emptyList()
        lines.drop(1).forEach { line ->
            try {
                val flightMap = keys.zip(line.split("\t")).toMap()
                addDuty(
                    if (flightMap[IS_SIM]?.toInt() == 1)
                        flightMapToSim(flightMap)
                    else
                        flightMapToFlight(flightMap)
                )
            } catch (e: Exception) {
                throw ParsingException("Cannot parse line $line", e)
            }
        }
    }

    private fun flightMapToSim(flightMap: Map<String, String>): ParsedSimulatorDuty {
        val date = LocalDate.parse(flightMap[MCC_DATE])

        return ParsedSimulatorDuty(
            date = date,
            duration = getDuration(flightMap[TIME_TOTALSIM]),
            simulatorType = flightMap[AC_MODEL]?: "",
            remarks = flightMap[REMARKS],
            persons = getOtherNames(flightMap)
        )
    }

    private fun flightMapToFlight(flightMap: Map<String, String>): ParsedFlight {
        val date = LocalDate.parse(flightMap[MCC_DATE])
        val depTime = LocalTime.parse(flightMap[TIME_DEP]!!).atDate(date)
        val arrTime = LocalTime.parse(flightMap[TIME_ARR]!!).atDate(date).let{
            if (it < depTime)
                it.plusDays(1)
            else it
        }

        return ParsedFlight(
            flightNumber = flightMap[FLIGHTNUMBER],
            takeoffAirport = flightMap[AF_DEP]!!,
            landingAirport = flightMap[AF_ARR]!!,
            date = date,
            departureTime = depTime,
            arrivalTime = arrTime,
            overriddenTotalTime = getDuration(flightMap[TIME_TOTAL]),
            nightTime = getDuration(flightMap[TIME_NIGHT]),
            ifrTime = getDuration(flightMap[TIME_ACTUAL]),
            aircraftType = flightMap[AC_MODEL],
            aircraftRegistration = flightMap[AC_REG],
            numberOfTakeoffsByDay = flightMap[TO_DAY]?.takeIf { it.isNotBlank() }?.toInt(),
            numberOfTakeoffsByNight = flightMap[TO_NIGHT]?.takeIf { it.isNotBlank() }?.toInt(),
            numberOfLandingsByDay = flightMap[LDG_DAY]?.takeIf { it.isNotBlank() }?.toInt(),
            numberOfLandingsByNight = flightMap[LDG_NIGHT]?.takeIf { it.isNotBlank() }?.toInt(),

            pilotInCommand = flightMap[PILOT1_NAME]?.let { Person.fromString(it)},
            personsNotPIC = getOtherNames(flightMap),
            isPICDuty = getDuration(flightMap[TIME_PIC]) > 0.minutes,
            isPICUSDuty = getDuration(flightMap[TIME_PICUS]) > 0.minutes,
            isCopilotDuty = getDuration(flightMap[TIME_SIC]) > 0.minutes,
            isDualDuty = getDuration(flightMap[TIME_DUAL]) > 0.minutes,
            isPF = flightMap[IS_PF]?.let { it != "0"},
            remarks = flightMap[REMARKS],
        )
    }

    /**
     * Combines the values in NAME2, NAME3 and NAME4 into a List
     */
    private fun getOtherNames(flightMap: Map<String, String>): List<Person> =
        listOfNotNull(flightMap[PILOT2_NAME], flightMap[PILOT3_NAME], flightMap[PILOT4_NAME]).map{
            Person.fromString(it)
        }

    private fun getDuration(durationString: String?): Duration {
        if (durationString.isNullOrBlank()) return 0.minutes
        if(':' !in durationString) return durationString.toInt().minutes
        val splits = durationString.split(":")
        return splits[0].toInt().hours + splits[1].toInt().minutes
    }


    companion object : CSVParserConstructor {
        private const val TEXT_TO_SEARCH_FOR = "mcc_DATE\tIS_PREVEXP\tAC_ISSIM"

        override fun createIfAble(csvLines: List<String>): MccPilotLogParser? =
            if (firstLineWithoutQuotesMatches(csvLines))
                MccPilotLogParser(csvLines)
            else null

        private fun firstLineWithoutQuotesMatches(lines: List<String>) =
            (lines.firstOrNull()?.filter { it != '\"' } ?: "").startsWith(TEXT_TO_SEARCH_FOR, ignoreCase = true)


        // constants to find values to parse into ParsedFlight
        private const val MCC_DATE = "MCC_DATE"
        private const val IS_SIM = "AC_ISSIM"
        private const val FLIGHTNUMBER = "FLIGHTNUMBER"
        private const val AF_DEP = "AF_DEP"
        private const val AF_ARR = "AF_ARR"
        private const val TIME_DEP = "TIME_DEP"
        private const val TIME_ARR = "TIME_ARR"
        private const val TIME_TOTAL = "TIME_TOTAL"

        private const val AC_MODEL = "AC_MODEL"
        private const val AC_REG = "AC_REG"
        private const val PILOT1_NAME = "PILOT1_NAME"
        private const val PILOT2_NAME = "PILOT2_NAME"
        private const val PILOT3_NAME = "PILOT3_NAME"
        private const val PILOT4_NAME = "PILOT4_NAME"
        private const val TIME_PIC = "TIME_PIC"
        private const val TIME_PICUS = "TIME_PICUS"
        private const val TIME_SIC = "TIME_SIC"
        private const val TIME_DUAL = "TIME_DUAL"
        private const val TIME_NIGHT = "TIME_NIGHT"
        private const val TIME_ACTUAL = "TIME_ACTUAL"
        private const val TIME_TOTALSIM = "TIME_TOTALSIM"
        private const val TO_DAY = "TO_DAY"
        private const val TO_NIGHT = "TO_NIGHT"
        private const val LDG_DAY = "LDG_DAY"
        private const val LDG_NIGHT = "LDG_NIGHT"
        private const val IS_PF = "IS_PF"
        private const val REMARKS = "REMARKS"
    }
}
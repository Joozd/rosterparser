package nl.joozd.rosterparser.parsers.pdf

import com.itextpdf.text.pdf.PdfReader
import nl.joozd.rosterparser.AirportFormat
import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedRoster
import nl.joozd.rosterparser.ParsingException
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Parse a KLC Monthly overview to flights.
 * Does not support simulator duties at the moment (ignores them)
 */
class KlcRosterParser(private val lines: List<String>) : PDFParser() {
    private val dateRangeRegEx = """Period: ($DAY_REGEX_STRING) - ($DAY_REGEX_STRING) contract:""".toRegex()
    private val dayRegex = """^(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun)(\d\d).*""".toRegex()
    private val flightRegex = (
            "(?<${GroupNames.FLIGHT_NUMBER}>$CARRIER\\s\\d+)\\sR?\\s?" +     // flight number: CARRIER prefix and 1 or more digits. Might be followed by an R if requested.
                    "(?<${GroupNames.ORIG}>[A-Z]{3})\\s" +                  // Origin: 3 capital letters
                    "(?<${GroupNames.TIME_OUT}>\\d{4})\\s" +                // time out (departure): 4 digits
                    "(?<${GroupNames.TIME_IN}>\\d{4})\\s" +                 // time in (arrival): 4 digits
                    "(?<${GroupNames.DEST}>[A-Z]{3})\\s?" +                  // Destination: 4  capital letters
                    "(?<${GroupNames.AIRCRAFT}>\\w{3})?"                     // Aircraft type: 3 letters or digits. Can be absent.
            ).toRegex()

    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws ParsingException when the data used to construct this parser cannot be parsed after all
     */
    override fun getRoster(): ParsedRoster = ParsedRoster.build {
        val period = getPeriodFromLines(lines)
        airportFormat = AirportFormat.IATA
        rosterPeriod = period
        flightsArePlanned = true

        // get days (needed for dates as dates are not in flight line). A day is a List<List<String>>
        buildDaysFromLines(lines).forEach { day -> // day: List<String>
            val date = getDateFromDay(day, period)
            // get lines with flights from day
            day.drop(1).filter { it matches flightRegex }.forEach { flightLine ->
                addDuty(makeFlightFromLine(flightLine, date))
            }
        }
    }


    /**
     * Get the period of this roster from the lines that make up the roster
     */
    private fun getPeriodFromLines(lines: List<String>): ClosedRange<LocalDate> =
        dateRangeRegEx.find(lines.joinToString("\n"))?.let {
            makePeriodFromRegexResult(it)
        } ?: throw ParsingException("KlcRosterParser: Could not parse period from roster")


    /**
     * Make a ClosedRange<LocalDate> from a MatchResult with a date range in it
     */
    private fun makePeriodFromRegexResult(dateRangeResult: MatchResult): ClosedRange<LocalDate> {
        val startEnd = dateRangeResult.groups[1]!!.value to dateRangeResult.groups[2]!!.value
        val startOfRoster = LocalDate.parse(startEnd.first, DateTimeFormatter.ofPattern("ddMMMyy", Locale.US))
        val endOfRoster = LocalDate.parse(startEnd.second, DateTimeFormatter.ofPattern("ddMMMyy", Locale.US))
        return startOfRoster..endOfRoster
    }

    /**
     * Chop a roster into days
     */
    private fun buildDaysFromLines(lines: List<String>): List<List<String>> {
        val days = ArrayList<List<String>>()

        val currentDay = ArrayList<String>()

        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val l = iterator.next()
            if (l matches dayRegex) {
                days.add(currentDay.toList())
                currentDay.clear()
            }
            currentDay.add(l)
        }
        days.add(currentDay)
        return days.drop(1) // first entry is just header data or empty
    }

    /**
     * Get the date from a day, using the current period
     */
    private fun getDateFromDay(day: List<String>, period: ClosedRange<LocalDate>): LocalDate =
        day.firstOrNull()?.let {
            dayRegex.find(it)?.groupValues?.get(1)?.toInt()
        }?.let { dayOfMonth ->
            val todayCandidate = period.start.withDayOfMonth(dayOfMonth)
            return if (todayCandidate in period)
                todayCandidate
            else
                todayCandidate.plusMonths(1)
        } ?: throw ParsingException("Unable to get date from day")

    /**
     * Make a [ParsedFlight] from a flightLine
     */
    private fun makeFlightFromLine(line: String, date: LocalDate): ParsedFlight =
        flightRegex.find(line)?.let { r ->
            val tOutString = r.groups[GroupNames.TIME_OUT]?.value
                ?: throw ParsingException("Unable to parse time out from line $line")
            val tInString =
                r.groups[GroupNames.TIME_IN]?.value ?: throw ParsingException("Unable to parse time in from line $line")
            val flightNumber = r.groups[GroupNames.FLIGHT_NUMBER]?.value?.filter { it != ' ' }
                ?: throw ParsingException("Unable to parse flight number from line $line")
            val typeString = r.groups[GroupNames.AIRCRAFT]?.value // can be null

            val tOut = date.atTime(makeTime(tOutString))
            val tIn = date.atTime(makeTime(tInString))
                .let { if (it < tOut) it.plusDays(1) else it } // add a day if past midnight
            val type = aircraftTypes[typeString]

            return ParsedFlight(
                date = date,
                flightNumber = flightNumber,
                takeoffAirport = r.groups[KlcMonthlyParser.GroupNames.ORIG]?.value
                    ?: throw ParsingException("Unable to parse departure airport from line $line"),
                landingAirport = r.groups[KlcMonthlyParser.GroupNames.DEST]?.value
                    ?: throw ParsingException("Unable to parse arrival airport from line $line"),
                departureTime = tOut,
                arrivalTime = tIn,
                aircraftType = type,
                isDeadHeading = flightNumber.startsWith("DH")
            )
        } ?: throw ParsingException("flightRegex did not have a match in line $line")

    /**
     * Make a LocalTime from a time like "1210"
     */
    private fun makeTime(timeString: String): LocalTime {
        val t = timeString.toInt()
        val hh = t / 100
        val mm = t % 100
        return LocalTime.of(hh, mm)
    }

    companion object : PDFParserConstructor {
        private const val LINE_TO_LOOK_AT = 1
        private const val TEXT_TO_SEARCH_FOR = "Individual duty plan for"

        override fun createIfAble(pdfLines: List<String>, pdfReader: PdfReader): KlcRosterParser? =
            if (LINE_TO_LOOK_AT in pdfLines.indices && pdfLines[LINE_TO_LOOK_AT].startsWith(TEXT_TO_SEARCH_FOR))
                KlcRosterParser(pdfLines)
            else null.also{
                println("Could not make roster from ${pdfLines.joinToString("\n")}")
            }

        private const val DAY_REGEX_STRING =
            """\d\d[A-Z][a-z]{2}\d\d""" // 2 digits (day), Jan/Feb/Mar etc, 2 digits (year)
        private const val CARRIER = "(?:DH/[A-Z]{2}|WA|KL)" // either DH/[2 letters] for dead heading, WA or KL

        private val aircraftTypes = mapOf(
            "E75" to "E75L",
            "E90" to "E190",
            "295" to "E295"
        )
    }

    object GroupNames {
        const val FLIGHT_NUMBER = "flightNumber"
        const val TIME_OUT = "timeOut"
        const val ORIG = "orig"
        const val DEST = "dest"
        const val TIME_IN = "timeIn"
        const val AIRCRAFT = "type"
    }
}
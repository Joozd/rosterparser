package nl.joozd.rosterparser.parsers.pdf

import com.itextpdf.text.pdf.PdfReader
import nl.joozd.rosterparser.AirportFormat
import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedRoster
import nl.joozd.rosterparser.ParsingException
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Parse a KLC Monthly overview to flights.
 * Does not support simulator duties at the moment (ignores them)
 */
class KlcMonthlyParser(private val lines: List<String>) : PDFParser() {
    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws ParsingException when the data used to construct this parser cannot be parsed after all
     */
    override fun getRoster(): ParsedRoster = ParsedRoster.build {

        val period = getPeriodFromLines(lines)
        rosterPeriod = period
        airportFormat = AirportFormat.IATA
        flightsArePlanned = false

        /**
         * A line with a flight in a KLC Monthly conforms to this regex.
         */
        val flightRegEx = (
                "(?<${GroupNames.DAY}>\\d{1,2}) " +                             // Day of the month: 1 or 2 digits.
                        "(?<${GroupNames.FLIGHT_NUMBER}>[A-Z]{2}\\d{3,4}) " +   // Flight number: 2 letters followed by 3 or 4 digits.
                        "(?<${GroupNames.TIME_OUT}>\\d\\d:\\d\\d) " +           // Time out (departure): HH:mm format.
                        "(?<${GroupNames.ORIG}>[A-Z]{3}) " +                    // Origin airport code: 3 letters.
                        "(?<${GroupNames.DEST}>[A-Z]{3}) " +                    // Destination airport code: 3 letters.
                        "(?<${GroupNames.REGISTRATION}>[A-Z]{3}) " +            // Aircraft registration: 3 letters.
                        "(?<${GroupNames.TIME_IN}>\\d\\d:\\d\\d) " +            // Time in (arrival): HH:mm format.
                        "(?<${GroupNames.DURATION}>\\d\\d:\\d\\d)"              // Duration of flight: HH:mm format.
                ).toRegex()

        lines.mapNotNull { flightRegEx.find(it) }.forEach {
            addDuty(flightFromMatchResult(it, period))
        }

    }


    private fun flightFromMatchResult(matchResult: MatchResult, period: ClosedRange<LocalDate>): ParsedFlight {
        val timeOut = makeTimeOut(matchResult, period)
        val timeIn = makeTimeIn(matchResult, period, timeOut)
        val flightNumber = matchResult.groups[GroupNames.FLIGHT_NUMBER]?.value
            ?: throw ParsingException("KlcMonthlyParser error 6: No flight number in line ${matchResult.groupValues.first()}")
        val orig = matchResult.groups[GroupNames.ORIG]?.value
            ?: throw ParsingException("KlcMonthlyParser error 7: No origin found in line ${matchResult.groupValues.first()}")
        val dest = matchResult.groups[GroupNames.DEST]?.value
            ?: throw ParsingException("KlcMonthlyParser error 8: No destination found in line ${matchResult.groupValues.first()}")
        val registration = matchResult.groups[GroupNames.REGISTRATION]?.value?.let { "PH-$it" }
            ?: throw ParsingException("KlcMonthlyParser error 9: No registration found in line ${matchResult.groupValues.first()}") // adds "PH-" to registration

        return ParsedFlight(
            date = timeOut.toLocalDate(),
            departureTime = timeOut,
            arrivalTime = timeIn,
            takeoffAirport = orig,
            landingAirport = dest,
            flightNumber = flightNumber,
            aircraftRegistration = registration
        )
    }


    /**
     * Get period from [lines] or throw a [ParsingException] if none found.
     */
    private fun getPeriodFromLines(lines: List<String>): ClosedRange<LocalDate> {
        val periodLine = lines.firstOrNull { it.startsWith(PERIOD_LINE_IDENTIFIER) }
            ?: throw ParsingException("KlcMonthlyParser error 1: Could not get period from roster file, probably not a correct KLC Monthly overview provided")
        return getDateRangeFromLine(periodLine)
            ?: throw ParsingException("KlcMonthlyParser error 2: Could not get period from roster file, probably not a correct KLC Monthly overview provided")
    }

    /**
     * Formatter for time values following the "HH:mm" pattern in the context of a KLC Monthly Overview,
     * where time is represented in hours and minutes.
     */
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")


    /**
     * Get the day of month from this [matchResultForLine]
     */
    private fun dayOfMonth(matchResultForLine: MatchResult): Int =
        (matchResultForLine.groups[GroupNames.DAY]?.value?.toInt()
            ?: throw ParsingException("KlcMonthlyParser error 3: No day of month line ${matchResultForLine.groupValues.first()}"))

    /**
     * Get the moment of departure for this matchResultForLine
     */
    private fun makeTimeOut(matchResultForLine: MatchResult, period: ClosedRange<LocalDate>): LocalDateTime =
        LocalTime.parse(
            matchResultForLine.groups[GroupNames.TIME_OUT]?.value
                ?: throw ParsingException("KlcMonthlyParser error 4: No time out in line ${matchResultForLine.groupValues.first()}"),
            timeFormatter
        )
            .atDate(period.start.withDayOfMonth(dayOfMonth(matchResultForLine)))

    /**
     * Get the moment of arrival for this matchResultForLine
     * Needs timeOut in case of landing after midnight.
     */
    private fun makeTimeIn(
        matchResultForLine: MatchResult,
        period: ClosedRange<LocalDate>,
        timeOut: LocalDateTime
    ): LocalDateTime =
        LocalTime.parse(
            matchResultForLine.groups[GroupNames.TIME_IN]?.value
                ?: throw ParsingException("KlcMonthlyParser error 5: No time in in line ${matchResultForLine.groupValues.first()}"),
            timeFormatter
        )
            .atDate(period.start.withDayOfMonth(dayOfMonth(matchResultForLine)))
            .let {
                //add a day if landing before takeoff (means we went past midnight)
                if (it < timeOut) it.plusDays(1)
                else it
            }

    /**
     * Looks for two dates in [periodLine].
     * If it finds them, turns them into a ClosedRange<LocalDate>.
     * If it doesn't, returns null
     * @return the date range found in [periodLine], or null
     */
    private fun getDateRangeFromLine(periodLine: String): ClosedRange<LocalDate>? {
        val dateRegEx = """\d{2}-\d{2}-\d{4}""".toRegex()
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        // matches dd-MM-yyyy
        return dateRegEx.findAll(periodLine)
            .map { it.value }
            .toList()
            .takeIf { it.size == 2 } // only if exactly 2 dates found, else null.
            ?.map {
                // parse strings to dates
                LocalDate.parse(it, dateFormatter)
            }?.let {
                // dates to a ClosedRange<Date>
                it.first()..it.last()
            }
    }

    object GroupNames {
        const val DAY = "day"
        const val FLIGHT_NUMBER = "flightNumber"
        const val TIME_OUT = "timeOut"
        const val ORIG = "orig"
        const val DEST = "dest"
        const val REGISTRATION = "registration"
        const val TIME_IN = "timeIn"
        const val DURATION = "std"
    }


    companion object : PDFParserConstructor {
        private const val TEXT_TO_SEARCH_FOR =
            "MONTHLY OVERVIEW" // this seems rather generic? Might need something better

        /**
         * If [pdfLines] can be used to create this object, create it. Else, return null.
         * As a fallback option, [pdfReader] can be used for creation, but it should only be a last resort because
         * of performance concerns (parsing a PDF is expensive and there could be a lot of parsers trying to be created).
         * @param pdfLines The lines in this document as received from
         *  PdfTextExtractor.getTextFromPage(reader, page, SimpleTextExtractionStrategy()).lines()`
         *  (all pages concatenated). This is the prefered data to determine
         * @param pdfReader PdfReader object containing the PDF roster to be parsed, if able.
         *  Not recommended to use this for checking if the parser can be created due to performance reasons.
         */
        override fun createIfAble(pdfLines: List<String>, pdfReader: PdfReader): KlcMonthlyParser? =
            if ((pdfLines.firstOrNull() ?: "").startsWith(TEXT_TO_SEARCH_FOR))
                KlcMonthlyParser(pdfLines)
            else null

        const val PERIOD_LINE_IDENTIFIER = "Period: From "
    }
}
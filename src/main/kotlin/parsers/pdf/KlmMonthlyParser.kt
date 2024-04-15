package nl.joozd.rosterparser.parsers.pdf

import com.itextpdf.text.pdf.PdfReader
import nl.joozd.rosterparser.*
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Parses a Klm Monthly Overview to a ParsedRoster.
 * A KLM Monthly Overview contains completed flights.
 */
class KlmMonthlyParser(private val lines: List<String>) : PDFParser() {
    private val periodRegex = """Periode: ($DATE) t/m ($DATE)""".toRegex()
    private val flightLineMatcher =
        """^\d{2}\s+$FLIGHT_NUMBER\s+$REGISTRATION\s+$TIME.*""".toRegex() // only to check if line is a flight

    // create the matcher for simulator duties
    private val simMatches =
        arrayOf(SIM_TQ_MATCH, SIM_TQ_EXAM_MATCH, SIM_787_RECURRENT_MATCH, SIM_LOE_MATCH, SIM_TR_MATCH, SIM_LPC_MATCH)
    private val simLineMatcher = simMatches.joinToString(separator = "|").toRegex()

    private val tOutRegex = """($TIME)\s*(?:\+1)?\s*$AIRPORT""".toRegex()
    private val timeRegex = TIME.toRegex()
    private val airportRegex = AIRPORT.toRegex()

    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws ParsingException when the data used to construct this parser cannot be parsed after all
     */
    override fun getRoster(): ParsedRoster = ParsedRoster.build {
        val period = getPeriodFromLines(lines)
        val isCaptain = isCaptain(lines)
        val fixedLines = fixLines(lines) // sometimes notes can break a line in multiple lines. this is fixed here.

        val flightLines = fixedLines.filter { it matches flightLineMatcher }
        val simLines = fixedLines.filter { simLineMatcher.containsMatchIn(it) }

        flightLines.forEach {line ->
            addDuty(flightFromLine(line, period, isCaptain))
        }

        simLines.forEach { line->
            addDuty(simFromLine(line, period))
        }

        timeZone = ZoneOffset.UTC
        rosterPeriod = period
        airportFormat = AirportFormat.IATA
        flightsArePlanned = false
    }

    /**
     * Gets the period for this roster
     */
    private fun getPeriodFromLines(lines: List<String>): ClosedRange<LocalDate> =
        lines.firstOrNull { it.trim() matches periodRegex }?.let { periodLine ->
            getRangeFromPeriodLine(periodRegex.find(periodLine)!!)
        } ?: throw ParsingException("Could not get roster period from roster")

    /**
     * Get the date range from period line
     */
    private fun getRangeFromPeriodLine(matchResult: MatchResult): ClosedRange<LocalDate> {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val (start, end) = matchResult.destructured
        val startDate = LocalDate.parse(start, formatter)
        val endDate = LocalDate.parse(end, formatter)
        return startDate..endDate
    }

    /**
     * true if this is a captain's overview.
     */
    private fun isCaptain(lines: List<String>): Boolean {
        val postingLine = lines.firstOrNull { it.startsWith("Posting") }
            ?: throw ParsingException("KlmMonthlyParser: Missing Posting line, malformed data.") // find line that is like `Posting: FO - 778 Crew Type: Cockpit`
        val posting = postingLine.split(' ').takeIf { it.size > 1 }
            ?: throw ParsingException("KlmMonthlyParser: No spaces in Posting line, malformed data.") // split line in words. If not at least 2 words in line, return false
        return posting[1] == "CP" // if the second word in this line is "CP", return true, if it is anything else return false
    }


    private val dayLine = """^\d{2}\s+.*""".toRegex()

    /**
     * Sometimes a line can get broken in two, for instance on my initial linecheck:
     * 31 KL 714 PHBVS 18:30 20:15 PBM -3 SO AMS LCI,
     * LC
     * +1 04:50 +1 05:20 +1 8:35 10:50 21:12 10:50 10:20
     * This function adds all lines that do not start with two digits and a space (i.e. a day-of-month) to the previous line, fixing that.
     */
    private fun fixLines(lines: List<String>): List<String> = buildList {
        var currentLine = ""
        lines.forEach { line ->
            if (line matches dayLine) {
                add(currentLine)
                currentLine = line
            } else {
                currentLine += line
            }
        }
        add(currentLine)
    }

    /**
     * Make a flight from a line.
     * @throws ParsingException on error.
     */
    private fun flightFromLine(line: String, dateRange: ClosedRange<LocalDate>, isCaptain: Boolean): ParsedFlight {
        try {
            val words = line.split(" ").map { it.trim() }.filter { it.isNotBlank() }
            //this function expects a trimmed line
            /*
            examples:
            09 KL 831 PHBHO   05:30 06:31 ICN +9 FO HGH +8 08:55 2:24 46:47
            09 KL 832 PHBHO   12:21 HGH +8 FO ICN +9 14:18 14:48 1:57 9:18 9:18 8:48
            10 KL 868 PHBHF   14:20 15:29 ICN +9 FO AMS +1 05:15 +1 05:45 +1 13:46 15:25 23:32 15:25 14:55
            16 KL 714D PHBVV 17:40 19:11 PBM -3 FO AMS LCI +2 03:36 +1 04:06 +1 8:25 10:26 19:25 10:26 9:56
         */
            val dayOfMonth = line.take(2).toInt() // first two digits make day of month
            val date = dateRange.start.withDayOfMonth(dayOfMonth)
            val flightNumber = words[1] + words[2]
            val registration = words[3]
            val (orig, dest) = airportRegex.findAll(line).map { it.value.trim() }.toList()
            val flightPeriod = getFlightPeriod(line, date) // local times
            val timeZoneOffsets = AIRPORT_WITH_TIME_OFFSET.toRegex().findAll(line).map { it.groupValues[1].toLong() }.toList() // should have 2 results, the timeZone offsets, e.g.-3 or +11. Subtract this from the local time to get UTC.

            return ParsedFlight(
                date = date,
                flightNumber = flightNumber,
                departureTime = flightPeriod.start.minusHours(timeZoneOffsets[0]),
                arrivalTime = flightPeriod.endInclusive.minusHours(timeZoneOffsets[1]),
                aircraftRegistration = registration,
                takeoffAirport = orig,
                landingAirport = dest,
                isPICDuty = isCaptain
            )
        } catch (e: Exception){
            throw ParsingException("Could not crete Parsedflight from $line", e)
        }
    }

    /**
     * Make a sim duty from a line.
     * @throws ParsingException on error.
     */
    private fun simFromLine(line:String, dateRange: ClosedRange<LocalDate>): ParsedSimulatorDuty {
        try {
            val dayOfMonth = line.take(2).toInt() // first two digits make day of month
            val date = dateRange.start.withDayOfMonth(dayOfMonth)
            val description = when {
                // If this becomes a performance problem, I could do the compiling in the companion object or outside of this function. Don't think it will be though.
                SIM_TQ_MATCH.toRegex().containsMatchIn(line) -> "Type Qualification"
                SIM_TQ_EXAM_MATCH.toRegex().containsMatchIn(line) -> "Skill Test"
                SIM_787_RECURRENT_MATCH.toRegex().containsMatchIn(line) -> "787 Recurrent"
                SIM_LOE_MATCH.toRegex().containsMatchIn(line) -> "LOE"
                SIM_TR_MATCH.toRegex().containsMatchIn(line) -> "Type Recurrent"
                SIM_LPC_MATCH.toRegex().containsMatchIn(line) -> "OPC/LPC"
                else -> ""
            }
            val type = if (SIM_787_RECURRENT_MATCH.toRegex().containsMatchIn(line)) "B789" else ""

            return ParsedSimulatorDuty(
                date = date,
                duration = 3.hours + 30.minutes, // fixed duration, not present in overview
                simulatorType = type,
                remarks = description
            )
        } catch (e: Exception){
            throw ParsingException("Unable to parse simulator duty from $line", e)
        }
    }

    /**
     * Get the flight period (off blocks to on blocks) for a [line], combined with a [reportingDate].
     */
    private fun getFlightPeriod(line: String, reportingDate: LocalDate): ClosedRange<LocalDateTime> { // period from off to on blocks
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val tOutMatchResult = tOutRegex.find(line) ?: throw(IllegalArgumentException("No tOut found in $line"))
        val lineAfterTOut = line.drop(tOutMatchResult.range.last)
        //The first time after timeOut is timeIn.
        val tInMatchResult = timeRegex.find(lineAfterTOut) ?: throw(IllegalArgumentException("No tIn found in $lineAfterTOut"))

        val tOut = LocalTime.parse(tOutMatchResult.groupValues[1], formatter)
        val tIn = LocalTime.parse(tInMatchResult.value, formatter)

        // If departure date is after reporting date (e.g. reporting just before midnight UTC), there is a "+1" in tOutMatchResult
        val dateOut = if ("+1" in tOutMatchResult.value) reportingDate.plusDays(1) else reportingDate

        // Flight time is always 0..24 hours. If timeIn is before timeOut, we add a day.
        // There is data in the chrono about that, but this is easier.
        val dateIn = if(tIn > tOut) dateOut else dateOut.plusDays(1)

        val dateTimeOut = tOut.atDate(dateOut)
        val dateTimeIn = tIn.atDate(dateIn)

        return dateTimeOut..dateTimeIn
    }


    companion object : PDFParserConstructor {
        private const val TEXT_TO_SEARCH_FOR = "VOOR VRAGEN OVER VLIEGUREN: FLIGHTCREWSUPPORT@KLM.COM"

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
        override fun createIfAble(pdfLines: List<String>, pdfReader: PdfReader): KlmMonthlyParser? =
            if (pdfLines.any { TEXT_TO_SEARCH_FOR in it })
                KlmMonthlyParser(pdfLines)
            else null

        private const val DATE = """\d{2}-\d{2}-\d{4}"""

        private const val TIME = """\d\d:\d\d"""
        private const val REGISTRATION = """[A-Z]{5}"""
        private const val FLIGHT_NUMBER = """KL\s+\d{3,4}[dD]?"""
        private const val AIRPORT =
            """\s[A-Z]{3}\s""" // 3 capital letters with whitespace before or after is always an airport.

        private const val AIRPORT_WITH_TIME_OFFSET = """\s[A-Z]{3}\s+([+-]\d{1,2})\s""" // group is the time offset

        // sim identifiers are all regexes.
        // The check for a digit after 0 or more whitespaces (at the end) is to filter out instruction.
        private const val SIM_TQ_MATCH = """VK - Type Kwalificatie sim\s*\d"""
        private const val SIM_TQ_EXAM_MATCH = """VXN - Type Kwalificatie\s*\d"""
        private const val SIM_787_RECURRENT_MATCH = """VT8 - 787 RECURRENT\s*\d"""
        private const val SIM_LOE_MATCH = """VS?A - LOE\s*\d"""
        private const val SIM_TR_MATCH = """VT\d - Type recurrent \d\s*\d"""
        private const val SIM_LPC_MATCH = """VC - OPC / LPC\s*\d"""
    }
}
package nl.joozd.rosterparser.parsers.pdf

import com.itextpdf.text.pdf.PdfReader
import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedRoster
import nl.joozd.rosterparser.ParsingException
import nl.joozd.rosterparser.Person
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Parses a KLC Briefing Sheet to a ParsedRoster.
 */
class KlcBriefingSheetParser(private val lines: List<String>): PDFParser() {
    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws ParsingException when the data used to construct this parser cannot be parsed after all
     */
    override fun getRoster(): ParsedRoster = ParsedRoster.build {
        timeZone = ZoneOffset.UTC
        flightsArePlanned = true

        val flightsWithoutCrew = getFlightsLines(lines).map{ dutyFromLine(it) }
        val crewLines = getCrewLines(lines)
        val personOnRoster = getPersonOnRoster(lines)

        addNamesToFlights(flightsWithoutCrew, crewLines, personOnRoster).forEach{
            addDuty(it)
        }

    }

    /**
     * Returns a list of all lines that contain a Flight
     */
    private fun getFlightsLines(lines: List<String>): List<String> {
        val start = lines.indexOf(START_OF_FLIGHTS)+1
        val end = lines.indexOf(END_OF_FLIGHTS)
        if (start < 0  || end < 0) throw ParsingException("KlcBriefingSheetParser: Could not find START_OF_FLIGHTS and/or END_OF_FLIGHTS in roster lines (error 1)")
        // Only lines from START_OF_FLIGHTS to END_OF_FLIGHTS
        return lines.subList(start, end)
            .filter{l ->
                // ONly lines for which the word on index TIME_OUT is all digits. Other lines are not flights.
                l.split(' ').getOrNull(TIME_OUT)?.all{it.isDigit()}
                    ?: false
            }
    }

    /**
     * This grabs the part starting with "Crew Info" and then returns all lines with names information
     * (= all lines starting with a date + flightnumber)
     *
     * Example data:
     *
     * Crew Info
     * Date Flight CP FO SE ST
     * PREV EVENT NO PREV EVENT NO PREV EVENT NO PREV EVENT NO PREV EVENT
     * 17Feb KL1199 44692 WELLE, JOOST 59852 JONGEPIER, SHAHANE 67769 CROES, TESSA 42919 JAHNIG, RON
     * 18Feb KL1196
     * 19Feb KL1554 44692 59852 67769 42919
     * 19Feb KL1739
     * 19Feb KL1740
     * NEXT EVENT NO NEXT EVENT NO NEXT EVENT NO NEXT EVENT NO NEXT EVENT
     * ADDITIONAL INFORMATION
     */
    private fun getCrewLines(lines: List<String>): List<String> {
        if (START_OF_CREW_INFO !in lines) throw ParsingException("KlcBriefingSheetParser: Could not find START_OF_CREW_INFO  in roster lines (error 2)")
        // Matches lines starting with a date (eg 17Feb)
        val nameRegex = """^\d\d[A-Z][a-z]{2}.*""".toRegex()
        return lines.drop(lines.indexOf(START_OF_CREW_INFO))
            .filter { it matches nameRegex }
    }


    /**
     * gets a ParsedFlight from a [flightLine]. No sim duties on this roster type.
     * Example flight line:
     * 03Jan KL1781 KLM63J AMS 1205 HAM 1310 01:05 00:35 - 0 E90/PHEZY 1754 1119 1782 1345
     */
    private fun dutyFromLine(flightLine: String): ParsedFlight{
        val words = flightLine.split(' ')
        val date = getDate(words[DATE])
        val tOut = getTime(date, words[TIME_OUT])
        val tIn = getTimeIn(words[TIME_IN], tOut)

        return ParsedFlight(
            date = date,
            flightNumber = words[FLIGHTNUMBER],
            takeoffAirport = words[ORIG],
            landingAirport = words[DEST],
            departureTime = tOut,
            arrivalTime = tIn,
            aircraftRegistration = getRegistration(flightLine),
        )
    }

    /**
     * The registration is the first occurrence of a slash followed by 5 capital letters in a row on a [flightLine]
     */
    private fun getRegistration(flightLine: String) = "/([A-Z]{5})".toRegex().find(flightLine)?.groupValues?.get(1) ?: ""

    private fun getTime(date: LocalDate, timeString: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("HHmm")
        val time = LocalTime.parse(timeString, formatter)
        return LocalDateTime.of(date, time)
    }

    private fun getTimeIn(timeString: String, tOut: LocalDateTime): LocalDateTime {
        val t = getTime(tOut.toLocalDate(), timeString)
        return if (t > tOut) t else t.plusDays(1)
    }

    private fun getDate(dateString: String): LocalDate {
        val dateFormat = DateTimeFormatter.ofPattern("ddMMM", Locale.US)
        return MonthDay.parse(dateString, dateFormat).atYear(Year.now().value)
    }


    /**
     * Add names found in [crewLines] to [flights]
     * If name of first pilot on a flight == [personOnRoster], set the flight to isPICDuty
     * @return a new list of ParsedFlight objects
     *
     */
    private fun addNamesToFlights(flights: List<ParsedFlight>, crewLines: List<String>, personOnRoster: Person): List<ParsedFlight> = buildList{
        val crewPsnToNameMap = makeCrewPsnToNameMap(crewLines)

        // List of most recently found crew, to be used until a new crew is found
        var currentCrew = emptyList<Person>()

        crewLines.forEach{ line ->
            /*
             * Every line is a flight
             * examples:
             * 04Sep KL991 44692 53917 63034 74061 DINTEN VAN SPIJK, REBECCA
             * 04Sep KL992
             *
             * If there are numbers in a line, that means a crew change.
             */
            // update currentCrew
            if (line.getNumberStrings().isNotEmpty()){
                currentCrew = line.getNumberStrings().map{ psn -> crewPsnToNameMap[psn] ?: Person("ERROR")} // this error does actually happen but the rest of the roster is still valid, so no exception I think. See https://github.com/Joozd/LogbookApp/issues/4
            }
            val pic = currentCrew.firstOrNull()
            val otherPersons = currentCrew.drop(1).takeIf { it.isNotEmpty() }

            //Now, we have all names for the flight that goes with this line. Next: Find that flight


            //dateString = 09Sep, flightNumber = KL123
            val (dateString, flightNumber) = line.split(' ').let {it[0] to it[1]}
            val date = getDate(dateString)

            flights.firstOrNull{it.date == date && it.flightNumber == flightNumber}?.let{
                add(it.copy(pilotInCommand = pic,
                    personsNotPIC = otherPersons,
                    isPICDuty = pic.toString() == personOnRoster.toString()) // compare string representations as ID is not added to myName
                )
            }
        }
    }

    /**
     * Build a map of PSN numbers to crew member names
     */
    private fun makeCrewPsnToNameMap(crewLines: List<String>?): Map<String, Person> =
        HashMap<String,Person>().apply {
            crewLines?.forEach { line ->
                putNames(line)
            }
        }

    /**
     * Insert all persons in a line into a map, with their PSN as key
     */
    private fun MutableMap<String, Person>.putNames(line: String) {
        val numbers = line.getNumberStrings()
        if (numbers.isEmpty()) return
        numbers.indices.forEach { i ->
            val n = numbers[i]
            val raw = if (i != numbers.indices.last) {
                val next = numbers[i + 1]
                //raw is all text between this number and the next
                line.substring(line.indexOf(n) + n.length until line.indexOf(next)).trim()
            }
            else line.drop(line.indexOf(n) + n.length).trim()

            if (raw.isNotBlank())
                set(numbers[i], rawNameToPerson(raw, numbers[i]))
        }
    }

    /**
     * Gets all words that are only numbers:
     * "hallo ab123 456 7" will return [456, 7]
     */
    private fun String.getNumberStrings(): List<String> = split(' ')
        .filter{ s -> s.all { it.isDigit() } }


    /**
     * Takes a RawName ("JANSEN, JAN", of "VRIES, VAN DE, HENK"
     * and turns it into a written name like "Henk van de Vries" (capitalized except for "tussenvoegsel")
     */
    private fun rawNameToPerson(rawName: String, id: String? = null): Person {
        val nameParts = rawName.split(',').map{ it.trim() }
        return when(nameParts.size){
            0 -> Person(id = id)
            1 -> Person(id = id, lastName = capitalizeAllWords(nameParts.first()))// John Wayne. Should not be there I think.
            2 -> Person(id = id, firstName = capitalizeAllWords(nameParts.last()), lastName = capitalizeAllWords(nameParts.first())) // "WAYNE BOBBITT, JOHN" -> "John Wayne Bobbitt"
            3 -> Person(id = id, firstName = capitalizeAllWords(nameParts.last()), prefix = nameParts[1].lowercase(), lastName = capitalizeAllWords(nameParts.first())) // "HAAR, TER, JAAP" -> "Jaap ter Haar"
            else -> Person(id = id) // no name, probably preferable to a ParsingException.
        }
    }

    /**
     * This string now starts with a capital.
     */
    private fun String.withCapital(): String = lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(
        Locale.ROOT) else it.toString() }


    /**
     * All Words In A Sentence Get A Capital Letter
     */
    private fun capitalizeAllWords(line: String): String = line.split(' ')
        .filter { it.isNotBlank() } // remove any extra spaces
        .joinToString(" ") {
            it.split('-')
                .joinToString("-") { it.withCapital() }
        }.trim()


    /**
     * Get name of pilot whose roster this is
     */
    private fun getPersonOnRoster(lines: List<String>): Person {
        val l = lines.first()
        val rawName = l.substring(MY_NAME_START.length..l.indexOf(MY_NAME_END)).trim().uppercase()
        return rawNameToPerson(rawName)
    }

    companion object: PDFParserConstructor{
        private const val LINE_TO_LOOK_AT = 0
        private const val TEXT_TO_SEARCH_FOR = "Cockpit Briefing for"


        override fun createIfAble(pdfLines: List<String>, pdfReader: PdfReader): KlcBriefingSheetParser? =
            // This might be too ambiguous but works for now.
            if (LINE_TO_LOOK_AT in pdfLines.indices && pdfLines[LINE_TO_LOOK_AT].startsWith(TEXT_TO_SEARCH_FOR))
                KlcBriefingSheetParser(pdfLines)
            else null


        private const val START_OF_FLIGHTS =
            "Date Flt Callsign Dep Arr Blk Grd Pax Payload Eq Prev Next Special Info CTOT"
        private const val END_OF_FLIGHTS = "Daily Summary"
        private const val START_OF_CREW_INFO = "Crew Info"
        private const val MY_NAME_START = "Cockpit Briefing for "
        private const val MY_NAME_END = " KLC AUTO BRIEFING"

        // Indices in split flight lines
        private const val DATE = 0
        private const val FLIGHTNUMBER = 1
        // callsign = 2
        private const val ORIG = 3
        private const val TIME_OUT = 4
        private const val DEST = 5
        private const val TIME_IN = 6
    }


}
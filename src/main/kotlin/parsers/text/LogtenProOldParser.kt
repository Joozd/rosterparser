package nl.joozd.rosterparser.parsers.text

import nl.joozd.rosterparser.*
import nl.joozd.rosterparser.parsers.TextParser
import nl.joozd.rosterparser.parsers.factories.TextParserConstructor
import nl.joozd.rosterparser.services.csv.CSVReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Extracts flights from old Logten Pro exports.
 * Not sure if they changed the format, but my file is 15 years old and from a verion that no longer exists.
 * This will throw a [ParsingException] if the input file has line breaks in text fields.
 */
class LogtenProOldParser(private val text: String) : TextParser() {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws ParsingException when the data used to construct this parser cannot be parsed after all
     */
    override fun getRoster(): ParsedRoster = ParsedRoster.build {
        airportFormat = AirportFormat.IATA
        timeZone = ZoneOffset.UTC
        flightsArePlanned = false
        // period is left at default (auto-set by Builder)

        val balancesForward = ArrayList<BalanceForward>()

        buildFlightsMapList(text).forEach {
            val parsedDuty = parseDuty(it)

            // BalanceForward duties get added at the end, because they need a calculated date.
            if (parsedDuty is BalanceForward)
                balancesForward.add(parsedDuty)
            else addDuty(parsedDuty)
        }
        // add Balances Forward dated at start of period (or today if no period)
        val periodStart: LocalDate = calculatePeriodFromDuties(addedDuties)    // get the current period of added flights
            .takeIf { addedDuties.isNotEmpty() }                               // only if duties are.
            ?.start                                                            // start of that period will be used
            ?: LocalDate.now()                                                 // if no duties present, date is today.

        // add balances forward with calculated date, if date was set to PLACEHOLDER_DATE
        balancesForward.forEach{ bf ->
            if (bf.date == PLACEHOLDER_DATE)
                addDuty(bf.copy(date = periodStart))
            else addDuty(bf)
        }
    }

    private fun parseDuty(duty: Map<String, String>): ParsedDuty {
        val isBalanceForward = seemsToBeBalanceForward(duty)
        if (isBalanceForward) return parseBalanceForward(duty)


        val isSim = (duty.getDurationOrNull(SIM_TIME_KEY) ?: 0.minutes) != 0.minutes
        return if (isSim) return parseSimDuty(duty)

        else parseFlightDuty(duty)
    }

    /**
     * true is this seems to be a Balance Forward record. A bit hard to see, so we'll have to do some guessing.
     */
    private fun seemsToBeBalanceForward(duty: Map<String, String>): Boolean{
        val remarks = duty[REMARKS_KEY] ?: ""
        if (BALANCE_FORWARD_REMARKS.any { it in remarks.uppercase()}) return true // if user marked it as such in remarks
        val orig = duty[ORIG_KEY]
        val dest = duty[DEST_KEY]
        if (BALANCE_FORWARD_ORIGIN_CANDIDATES.any { it in (orig ?: "")}
            && BALANCE_FORWARD_DESTINATION_CANDIDATES.any { it in (dest ?: "")}
            ) return true // if user marked it as such in origin and destination
        if (duty[ORIG_KEY] == null && (duty.getDurationOrNull(SIM_TIME_KEY) ?: 0.minutes) == 0.minutes)
            return true // not a sim duty and no origin filled out

        return false // in other cases, not a balance forward.
    }

    private fun parseBalanceForward(balanceForwardDuty: Map<String, String>): BalanceForward{
        with(balanceForwardDuty) {
            val date = getDate(this) ?: PLACEHOLDER_DATE
            val totalTime = getDurationOrNull(TOTAL_TIME_KEY)
            val picTime = getDurationOrNull(PIC_KEY)
            val nightTime = getDurationOrNull(NIGHT_TIME_KEY)
            val actualIfrTime = getDurationOrNull(ACTUAL_IFR_TIME_KEY)
            val totalIfrTime = getDurationOrNull(SIMULATED_IFR_TIME_KEY).let { simulatedIfrTime ->
                when {
                    simulatedIfrTime == null -> actualIfrTime   // if both are null, this will be null
                    actualIfrTime == null -> simulatedIfrTime   // if actual is null (and simulated isn't), use simulated
                    else -> simulatedIfrTime + actualIfrTime    // if neither are null, use the sum of both
                }
            }
            val xcTime = getDurationOrNull(XC_TIME_KEY)
            val dualReceivedTime = getDurationOrNull(DUAL_RECEIVED_TIME_KEY)
            val dualGivenTime = getDurationOrNull(DUAL_GIVEN_TIME_KEY)
            val multiPilotTime = getDurationOrNull(MULTI_PILOT_TIME_KEY)
            val simulatorTime = getDurationOrNull(SIM_TIME_KEY)
            val takeoffsDay = this[DAY_TAKEOFFS_KEY]?.toInt()
            val takeoffsNight = this[NIGHT_TAKEOFFS_KEY]?.toInt()
            val landingsDay = this[DAY_LANDINGS_KEY]?.toInt()
            val landingsNight = this[NIGHT_LANDINGS_KEY]?.toInt()
            val autoLands = this[AUTOLANDS_KEY]?.toInt()
            val aircraftType = this[AIRCRAFT_TYPE_KEY]
            val remarks = this[REMARKS_KEY]

            return BalanceForward(
                date = date,
                totalTime = totalTime,
                picTime = picTime,
                nightTime = nightTime,
                xcTime = xcTime,
                ifrTime = totalIfrTime,  // Use the combined or single IFR time
                actualIfrTime = actualIfrTime,
                dualReceivedTime = dualReceivedTime,
                dualGivenTime = dualGivenTime,
                multiPilotTime = multiPilotTime,
                simulatorTime = simulatorTime,
                numberOfTakeoffsByDay = takeoffsDay,
                numberOfTakeoffsByNight = takeoffsNight,
                numberOfLandingsByDay = landingsDay,
                numberOfLandingsByNight = landingsNight,
                numberOfAutolands = autoLands,
                aircraftType = aircraftType,
                remarks = remarks
            )
        }
    }

    /**
     * Parse a simulator duty from a line map
     */
    private fun parseSimDuty(simDuty: Map<String, String>): ParsedSimulatorDuty {
        with(simDuty) {
            val simTime = getDurationOrNull(SIM_TIME_KEY) ?: throw ParsingException("Cannot get sim time ($SIM_TIME_KEY) from line $this")
            if (simTime == 0.minutes) throw ParsingException("Trying to parse a sim duty but no sim time logged ($SIM_TIME_KEY): $this")
            val date = getDate(simDuty) ?: throw ParsingException(
                "No date ($DATE_KEY) found in line with first key ${
                    keys.first().toByteArray().toList()
                } $this"
            )
            val type = simDuty[AIRCRAFT_TYPE_KEY]
            // Joins FLIGHT_NUMBER and REMARKS fields, because some people put session info in FLIGHT_NUMBER for simulator duties. Null if both blank or null.
            val remarks = listOfNotNull(this[FLIGHT_NUMBER_KEY], this[REMARKS_KEY]).joinToString(";").takeIf { it.isNotBlank() && it.trim() != ";" }
            val names = makeNamesList(this)

            return ParsedSimulatorDuty(
                date = date,
                duration = simTime,
                simulatorType = type,
                persons = names,
                remarks = remarks
            )
        }
    }


    /**
     * Parse a flight duty from a line map
     */
    private fun parseFlightDuty(flightDuty: Map<String, String>): ParsedFlight {
        with(flightDuty) {
            val date = getDate(flightDuty) ?: throw ParsingException("No date ($DATE_KEY) found in line $this")
            val origin = this[ORIG_KEY] ?: "" // throw ParsingException("Cannot get departure airport ($ORIG_KEY) from $flightDuty")
            val destination = this[DEST_KEY] ?: "" // throw ParsingException("Cannot get departure airport ($DEST_KEY) from $flightDuty")
            val timeOut =getTimeOrNull(DEPARTURE_TIME_KEY, date) ?: date.atStartOfDay() // throw ParsingException("Cannot get departure time ($DEPARTURE_TIME_KEY) from $this")
            val timeIn = (getTimeOrNull(ARRIVAL_TIME_KEY, date) ?: date.atStartOfDay()).let{
                if (it < timeOut) it.plusDays(1) else it
            } // throw ParsingException("Cannot get arrival time ($ARRIVAL_TIME_KEY) from $this")
            val correctedTotalTime = getDurationOrNull(TOTAL_TIME_KEY)
            val nightTime = getDurationOrNull(NIGHT_TIME_KEY)
            val ifrTime = getDurationOrNull(ACTUAL_IFR_TIME_KEY)
            val multiPilotTime = getDurationOrNull(MULTI_PILOT_TIME_KEY)
            val aircraft = get(AIRCRAFT_TYPE_KEY)
            val registration = get(AIRCRAFT_REG_KEY)
            val namePic = get(NAME_PIC)?.let { Person.fromString(it)}
            val otherNames = makeNamesList(this)
            val takeOffDay = get(DAY_TAKEOFFS_KEY)?.toInt()
            val takeOffNight = get(NIGHT_TAKEOFFS_KEY)?.toInt()
            val landingDay = get(DAY_LANDINGS_KEY)?.toInt()
            val landingNight = get(NIGHT_LANDINGS_KEY)?.toInt()
            val autoLand = get(AUTOLANDS_KEY)?.toInt()
            val flightNumber = get(FLIGHT_NUMBER_KEY)
            val remarks = get(REMARKS_KEY)

            // I am assuming boolean values are always 0 or 1. Any nonzero values (including negative) will be "true".
            val isPic = get(PIC_KEY)?.toInt()?.let { it != 0 }
            val isPicus = get(PICUS_KEY)?.toInt()?.let { it != 0 }
            val isCopilot = get(COPILOT_KEY)?.toInt()?.let { it != 0 }
            val isDual = get(DUAL_RECEIVED_TIME_KEY)?.toInt()?.let { it != 0 }
            val isInstructor = get(INSTRUCTOR_KEY)?.toInt()?.let { it != 0 }
            val isPF = get(PF_KEY)?.toInt()?.let { it != 0 }

            return ParsedFlight(
                date = date,
                departureTime = timeOut,
                arrivalTime =  timeIn,
                takeoffAirport = origin,
                landingAirport = destination,
                overriddenTotalTime = correctedTotalTime,
                nightTime = nightTime,
                ifrTime = ifrTime,
                multiPilotTime = multiPilotTime,
                aircraftType = aircraft,
                aircraftRegistration = registration,
                pilotInCommand = namePic,
                personsNotPIC = otherNames,
                numberOfTakeoffsByDay = takeOffDay,
                numberOfTakeoffsByNight = takeOffNight,
                numberOfLandingsByDay = landingDay,
                numberOfLandingsByNight = landingNight,
                numberOfAutolands = autoLand,
                flightNumber = flightNumber,
                remarks = remarks,
                isPICDuty = isPic,
                isPICUSDuty = isPicus,
                isCopilotDuty = isCopilot,
                isDualDuty = isDual,
                isInstructorDuty = isInstructor,
                isPF = isPF
            )
        }
    }

    private fun makeNamesList(flightDuty: Map<String, String>) =
        otherCrewKeys.mapNotNull { flightDuty[it] }.map { Person.fromString(it) }

    private fun getDate(simDuty: Map<String, String>): LocalDate? {
        return try {
            LocalDate.parse(
                (simDuty[DATE_KEY] ?: return null), // null if no date found.
                dateFormatter
            )
        } catch (dtpe: DateTimeParseException) {
            throw ParsingException("Cannot parse ${simDuty[DATE_KEY]}", dtpe)
        } catch (e: Throwable) {
            throw ParsingException("Exception while parsing date ${simDuty[DATE_KEY]}", e)
        }
    }

    /**
     * Get time from a field in a line map.
     */
    private fun Map<String, String>.getTimeOrNull(key: String, date: LocalDate): LocalDateTime? =
        get(key)?.let { timeString ->
            try {
                val lt = LocalTime.parse(timeString, timeFormatter)
                return lt.atDate(date)
            } catch (dtpe: DateTimeParseException) {
                throw ParsingException("Cannot parse $timeString", dtpe)
            } catch (e: Throwable) {
                throw ParsingException("Exception while parsing time $timeString", e)
            }
        }


    /**
     * Builds a list of line maps.
     */
    private fun buildFlightsMapList(csvData: String): List<Map<String, String>> =
        CSVReader(csvData, '\t')
            .generateRowMaps()

    /**
     * Gets number of minutes from a hh:mm string, treating it as a duration.
     */
    private fun minutesFromHoursAndMinutesString(s: String): Duration {
        require(s.count { it == ':' } == 1) { "only use minutesFromHoursAndMinutesString for hh:mm" }
        val (hrs, mins) = s.split(":").map { it.toInt() }
        return mins.minutes + (60 * hrs).minutes
    }

    /**
     * Get duration from [key] in a line map
     */
    private fun Map<String, String>.getDurationOrNull(key: String): Duration? = get(key)?.let { timeString ->
        when {
            timeString.isBlank() -> 0.minutes
            ':' in timeString -> minutesFromHoursAndMinutesString(timeString)
            else -> try {
                // It might be a fraction of hours, like "1.8". If this fails, throw an exception.
                val notANumberRegex = "[^0-9]".toRegex()
                val standardizedTimeString = timeString.replace(notANumberRegex, ".")
                val hours = standardizedTimeString.toDouble()
                (hours * 60).minutes
            } catch (e: Exception) {
                throw ParsingException("Exception while parsing minutes from $timeString", e)
            }
        }

    }

    companion object : TextParserConstructor {
        /**
         *  If [text] can be used to create this object, create it. Else, return null.
         */
        override fun createIfAble(text: String): TextParser? =
            text.take(maxOf(0, text.indexOf('\n'))).let { line -> //get all chars until first line break
                if (USED_KEYS.all { it in (line) })
                    LogtenProOldParser(text)
                else null
            }


        private const val DATE_KEY = "flight_flightDate"
        private const val DEPARTURE_TIME_KEY = "flight_actualDepartureTime"
        private const val ARRIVAL_TIME_KEY = "flight_actualArrivalTime"
        private const val ORIG_KEY = "flight_from"
        private const val DEST_KEY = "flight_to"
        private const val NAME_PIC = "flight_selectedCrewPIC"
        private const val TOTAL_TIME_KEY = "flight_totalTime"
        private const val PIC_TIME_KEY = "flight_pic"
        private const val XC_TIME_KEY = "flight_crossCountry"
        private const val NIGHT_TIME_KEY = "flight_night"
        private const val ACTUAL_IFR_TIME_KEY = "flight_actualInstrument"
        private const val SIMULATED_IFR_TIME_KEY = " flight_simulatedInstrument"
        private const val DUAL_RECEIVED_TIME_KEY = "flight_dualReceived"
        private const val DUAL_GIVEN_TIME_KEY = " flight_dualGiven"
        private const val SIM_TIME_KEY = "flight_simulator"
        private const val MULTI_PILOT_TIME_KEY = "flight_multiPilot"
        private const val AIRCRAFT_TYPE_KEY = "aircraftType_type"
        private const val AIRCRAFT_REG_KEY = "aircraft_aircraftID"
        private const val DAY_LANDINGS_KEY = "flight_dayLandings"
        private const val NIGHT_LANDINGS_KEY = "flight_nightLandings"
        private const val DAY_TAKEOFFS_KEY = "flight_dayTakeoffs"
        private const val NIGHT_TAKEOFFS_KEY = "flight_nightTakeoffs"
        private const val AUTOLANDS_KEY = "flight_autolands"
        private const val FLIGHT_NUMBER_KEY = "flight_flightNumber"
        private const val REMARKS_KEY = "flight_remarks"

        private const val PIC_KEY = "flight_picCapacity"
        private const val PICUS_KEY = "flight_underSupervisionCapacity"
        private const val COPILOT_KEY = "flight_sicCapacity"

        private const val INSTRUCTOR_KEY = "flight_selectedCrewInstructor"
        private const val PF_KEY = "flight_pilotFlyingCapacity"

        private val USED_KEYS = listOf(
            DATE_KEY,
            DEPARTURE_TIME_KEY,
            ARRIVAL_TIME_KEY,
            ORIG_KEY,
            DEST_KEY,
            NAME_PIC,
            TOTAL_TIME_KEY,
            PIC_TIME_KEY,
            XC_TIME_KEY,
            NIGHT_TIME_KEY,
            ACTUAL_IFR_TIME_KEY,
            SIMULATED_IFR_TIME_KEY,
            DUAL_RECEIVED_TIME_KEY,
            DUAL_GIVEN_TIME_KEY,
            SIM_TIME_KEY,
            MULTI_PILOT_TIME_KEY,
            AIRCRAFT_TYPE_KEY,
            AIRCRAFT_REG_KEY,
            DAY_LANDINGS_KEY,
            NIGHT_LANDINGS_KEY,
            DAY_TAKEOFFS_KEY,
            NIGHT_TAKEOFFS_KEY,
            AUTOLANDS_KEY,
            FLIGHT_NUMBER_KEY,
            REMARKS_KEY,
            PIC_KEY,
            PICUS_KEY,
            COPILOT_KEY,
            INSTRUCTOR_KEY,
            PF_KEY
        )

        private val BALANCE_FORWARD_REMARKS = listOf(
            "BALANCE FORWARD",
            "TOTALS FORWARD",
            "FROM PREVIOUS LOGBOOK"
        )

        private val BALANCE_FORWARD_ORIGIN_CANDIDATES = listOf(
            "BALANCE",
            "TOTALS"
        )

        private val BALANCE_FORWARD_DESTINATION_CANDIDATES = listOf(
            "FORWARD"
        )


        private val otherCrewKeys =
            "flight_selectedCrewSIC\t flight_selectedCrewRelief\t flight_selectedCrewRelief2\t flight_selectedCrewRelief3\t flight_selectedCrewRelief4\t flight_selectedCrewFlightEngineer\t flight_selectedCrewInstructor\t flight_selectedCrewStudent\t flight_selectedCrewObserver\t flight_selectedCrewObserver2\t flight_selectedCrewPurser\t flight_selectedCrewFlightAttendant\t flight_selectedCrewFlightAttendant2\t flight_selectedCrewFlightAttendant3\t flight_selectedCrewFlightAttendant4\t flight_selectedCrewCommander\t flight_selectedCrewCustom1\t flight_selectedCrewCustom2\t flight_selectedCrewCustom3\t flight_selectedCrewCustom4\t flight_selectedCrewCustom5".split(
                "\t"
            ).map { it.trim() }

        private val PLACEHOLDER_DATE = LocalDate.MIN
    }
}
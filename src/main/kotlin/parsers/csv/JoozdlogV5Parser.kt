package nl.joozd.rosterparser.parsers.csv

import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedRoster
import nl.joozd.rosterparser.ParsedSimulatorDuty
import nl.joozd.rosterparser.ParsingException
import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.factories.CSVParserConstructor
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.minutes

class JoozdlogV5Parser (private val lines: List<String>) : CSVParser() {
    private var periodStart: LocalDateTime? = null
    private var periodEnd: LocalDateTime? = null
    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws ParsingException when the data used to construct this parser cannot be parsed after all
     */
    override fun getRoster(): ParsedRoster = ParsedRoster.build {

        val keys = FINGERPRINT.split(";")
        lines.drop(1).forEach { line ->
            try {
                val flightMap = keys.zip(line.split(";")).toMap()
                addDuty(
                    if (flightMap["isSim"] == "true")
                        flightMapToSim(flightMap).also{
                            adjustRosterPeriodIfNeeded(it.date)
                        }
                    else
                        flightMapToFlight(flightMap).also{
                            adjustRosterPeriodIfNeeded(it.date)
                        }
                )
            } catch (e: Exception) {
                throw ParsingException("Cannot parse line $line", e)
            }
        }
        timeZone = ZoneOffset.UTC
        periodStart?.let { rosterStart = it }
        periodEnd?.let { rosterEnd = it }
        flightsArePlanned = false
    }

    /**
     * Adjusts the start and end of this roster if its date is outside previously known start and end.
     */
    private fun adjustRosterPeriodIfNeeded(date: LocalDate){
        if(periodStart == null  || date.atStartOfDay() < periodStart)
            periodStart = date.atStartOfDay()
        if(periodEnd == null || date.plusDays(1).atStartOfDay() > periodEnd)
            periodEnd = date.plusDays(1).atStartOfDay()
    }

    /**
     * Maps a flightMap to a ParsedSimulatorDuty
     */
    private fun flightMapToSim(flightMap: Map<String, String>): ParsedSimulatorDuty {
        val date = LocalDateTime.parse(flightMap["timeOut"]?.dropLast(1)).toLocalDate()
        val otherNames = getOtherNames(flightMap)

        return ParsedSimulatorDuty(
            date = date,
            duration = flightMap["simTime"]!!.toInt().minutes,
            simulatorType = flightMap["aircraftType"]!!,
            remarks = flightMap["remarks"]!!,
            names = otherNames,
            instructionGiven = flightMap["isInstructor"] == "true",
        )
    }

    /**
     * Maps a flightMap to a ParsedFlight
     */
    private fun flightMapToFlight(flightMap: Map<String, String>): ParsedFlight {
        val timeOut = LocalDateTime.parse(flightMap["timeOut"]?.dropLast(1))
        val timeIn = LocalDateTime.parse(flightMap["timeOut"]?.dropLast(1))
        val date = timeOut.toLocalDate()
        val correctedTotalTime = flightMap["correctedTotalTime"]!!.toInt().takeIf { it != 0 }?.minutes
        val multiPilotTime = flightMap["multiPilotTime"]!!.toInt().takeIf { it != 0 }?.minutes
        val nightTime = flightMap["nightTime"]!!.toInt().takeIf { it != 0 }?.minutes
        val ifrTime = flightMap["ifrTime"]!!.toInt().takeIf { it != 0 }?.minutes
        val otherNames = getOtherNames(flightMap)
        val isPIC = flightMap["isPIC"] == "true"

        // get augmentedCrew int, if it is not 0, make an AugmentedCrew object from it
        val augmentedCrewData =
            if (isPIC) null // PIC does not get rest so no augmented crew data will be saved
            else flightMap["augmentedCrew"]!!.toInt().takeIf { it != 0}?.let { AugmentedCrew.fromInt(it) }
        val crewSize = if (augmentedCrewData?.isFixedTime == false) augmentedCrewData.size else null // fixed rest times do not get a crew size
        val atControlsForTakeoff = if (augmentedCrewData?.isFixedTime == false) augmentedCrewData.takeoff else null // fixed rest times do not get didtakeoff info
        val atControlsForLanding = if (augmentedCrewData?.isFixedTime == false) augmentedCrewData.landing else null // fixed rest times do not get didlanding info
        val augmentedCrewTimeForTakeoffLanding = if (augmentedCrewData?.isFixedTime == false) augmentedCrewData.times else null // fixed rest times do not get didlanding info
        val augmentedCrewFixedRestTime = augmentedCrewData?.times?.takeIf { augmentedCrewData.isFixedTime } // only use fixed time if its flag is set

        return ParsedFlight(
            flightNumber = flightMap["flightNumber"]!!,
            takeoffAirport = flightMap["Origin"]!!,
            landingAirport = flightMap["dest"]!!,
            date = date,
            departureTime = timeOut,
            arrivalTime = timeIn,
            overriddenTotalTime = correctedTotalTime,
            nightTime = nightTime,
            multiPilotTime = multiPilotTime,
            ifrTime = ifrTime,
            aircraftType = flightMap["aircraftType"]!!,
            aircraftRegistration = flightMap["registration"]!!,
            numberOfTakeoffsByDay = flightMap["takeOffDay"]!!.toInt(),
            numberOfTakeoffsByNight = flightMap["takeOffNight"]!!.toInt(),
            numberOfLandingsByDay = flightMap["landingDay"]!!.toInt(),
            numberOfLandingsByNight = flightMap["landingNight"]!!.toInt(),

            // augmented crew data can stay null if augmentedCrewData == null
            crewSize = crewSize,
            atControlsForTakeoff = atControlsForTakeoff,
            atControlsForLanding = atControlsForLanding,
            augmentedCrewTimeForTakeoffLanding = augmentedCrewTimeForTakeoffLanding?.minutes,
            augmentedCrewFixedRestTime = augmentedCrewFixedRestTime?.minutes,

            namePIC = flightMap["name"]!!,
            namesNotPIC = otherNames,
            isPICDuty = isPIC,
            isPICUSDuty = flightMap["isPICUS"] == "true",
            isCopilotDuty = flightMap["isCoPilot"] == "true",
            isInstructorDuty = flightMap["isInstructor"] == "true",
            isDualDuty = flightMap["isDual"] == "true",
            remarks = flightMap["remarks"]!!,
            signatureSVG = flightMap["signatureSVG"]!!
        )

    }

    private fun getOtherNames(flightMap: Map<String, String>) = flightMap["name2"]!!.split("|")


    companion object : CSVParserConstructor {
        override fun createIfAble(csvLines: List<String>): CSVParser? =
            if (canCreateFromLines(csvLines)) JoozdlogV5Parser(csvLines)
            else null

        private fun canCreateFromLines(lines: List<String>): Boolean =
            lines.firstOrNull() == FINGERPRINT

        private const val FINGERPRINT =
            "flightID;Origin;dest;timeOut;timeIn;correctedTotalTime;multiPilotTime;nightTime;ifrTime;simTime;aircraftType;registration;name;name2;takeOffDay;takeOffNight;landingDay;landingNight;autoLand;flightNumber;remarks;isPIC;isPICUS;isCoPilot;isDual;isInstructor;isSim;isPF;isPlanned;autoFill;augmentedCrew;signatureSVG"
    }

    /**
     * Snapshot copied from JoozdLog. This version works for the V5 Joozdlog CSVs and decodes the "augmented crew" information.
     */
    private data class AugmentedCrew(
        val isFixedTime: Boolean = false,
        val size: Int = 2, // this gets ignored for calculations if isFixedTime is true
        val takeoff: Boolean = true,
        val landing: Boolean = true,
        val times: Int = 0,
        val isUndefined: Boolean = false)
    {
        /**
         * @See [fromInt] for format
         */
        fun toInt():Int {
            var value = if (size > MAX_CREW_SIZE) MAX_CREW_SIZE else size
            value = value.setBit(3, isFixedTime)
            value = value.setBit(4, takeoff).setBit(5, landing)
            value += times.shl(6)
            return value
        }

        /**
         * A crew is augmented if it is done by more than 2 pilots, or if a fixed rest time is entered.
         */
        val isAugmented get() = size > 2 || (isFixedTime && times != 0)

        /**
         * Returns a [AugmentedCrew] object with [isFixedTime] set to [fixedTime]
         */
        @Suppress("unused")
        fun withFixedRestTime(fixedTime: Boolean): AugmentedCrew = this.copy(isFixedTime = fixedTime)

        /**
         * Returns a [AugmentedCrew] object with [size] set to [crewSize]
         */
        @Suppress("unused")
        fun withCrewSize(crewSize: Int): AugmentedCrew = this.copy (size = crewSize)

        /**
         * Returns a [AugmentedCrew] object with [landing] set to [didLanding]
         */
        fun withLanding(didLanding: Boolean): AugmentedCrew = this.copy (landing = didLanding)

        /**
         * Returns a [AugmentedCrew] object with [takeoff] set to [didTakeoff]
         */
        fun withTakeoff(didTakeoff: Boolean): AugmentedCrew = this.copy (takeoff = didTakeoff)

        /**
         * Returns a [AugmentedCrew] object with [times] set to [newTimes]
         */
        fun withTimes(newTimes: Int): AugmentedCrew = this.copy (times = newTimes)


        /**
         * Return amount of time to log. Cannot be negative, so 3 man ops for a 20 min flight with 30 mins to/landing is 0 minutes to log.
         * @param totalTime time in minutes
         * @param pic: this flight is done as PIC
         */
        fun getLogTime(totalTime: Int, pic: Boolean): Int{
            if (pic) return maxOf (0, totalTime) // PIC logs all time
            if (isFixedTime) return maxOf (0, totalTime - times) // fixed rest time gets subtracted from
            if (size <=2) return maxOf (0, totalTime) // 2 or less crew logs all time

            val divideableTime = (totalTime - 2*times).toFloat()
            val timePerShare = divideableTime / size
            val minutesInSeat = (timePerShare*2).toInt()
            return maxOf (minutesInSeat + (if(takeoff) times else 0) + (if (landing) times else 0), 0)
        }

        fun getLogTime(totalTime: Duration, pic: Boolean): Int =
            getLogTime(totalTime.toMinutes().toInt(), pic)

        operator fun inc(): AugmentedCrew = this.copy (size = (size + 1).putInRange(MIN_CREW_SIZE..MAX_CREW_SIZE))

        operator fun dec(): AugmentedCrew = this.copy (size = (size - 1).putInRange(MIN_CREW_SIZE..MAX_CREW_SIZE))

        private fun Int.putInRange(range: IntRange): Int {
            require (!range.isEmpty()) { "cannot put an int in a range without elements"}
            return when {
                this in range -> this
                this < range.minOrNull()!! -> range.minOrNull()!!
                this > range.maxOrNull()!! -> range.maxOrNull()!!
                else -> error ("Value $this neither in our outside of $range...")
            }
        }



        companion object {
            const val MIN_CREW_SIZE = 1
            const val MAX_CREW_SIZE = 7

            fun coco(takeoffLandingTimes: Int): AugmentedCrew = AugmentedCrew(isFixedTime = false, size = 3, takeoff = false, landing = false, times = takeoffLandingTimes)
            fun fixedRest(fixedRestTime: Int): AugmentedCrew = AugmentedCrew(isFixedTime = true, times = fixedRestTime)

            /**
             * A value of 0 means "undefined"
             * - bits 0-2: amount of crew (no crews >7 )
             * - bit 3: if 1, this is a fixed time. If 0, time is calculated.
             * This reverse order from what makes sense is for backwards compatibility and easier migration.
             * - bit 4: in seat on takeoff
             * - bit 5: in seat on landing
             * - bit 6-31: amount of time reserved for takeoff/landing (standard in settings)
             */
            fun fromInt(value: Int) =
                if (value == 0) AugmentedCrew(times = 0, isUndefined = true) // 0 is not augmented.
                else AugmentedCrew(
                    size = 7.and(value),
                    isFixedTime = value.getBit(3),
                    takeoff = value.getBit(4),
                    landing = value.getBit(5),
                    times = value.ushr(6)
                )

            private fun Int.getBit(n: Int): Boolean {
                require(n < Int.SIZE_BITS-1) { "$n out of range (0 - ${Int.SIZE_BITS-2}" }
                return this.and(1.shl(n)) > 0 // its more than 0 so the set bit is 1, whatever bit it is
            }

            /**
             *  Set a bit in an Integer.
             *  Use value.setBit([0-31], [true/false])
             *  throws an IllegalArgumentException if requested bit doesn't exist
             *  @return the Integer with the bit set
             */
            private fun Int.setBit(n: Int, value: Boolean): Int{
                require(n < Int.SIZE_BITS) { "$n out of range (0 - ${Int.SIZE_BITS-1}" }
                return if (value)
                    this.or(1.shl(n))
                else
                    this.inv().or(1.shl(n)).inv()
            }
        }
    }
}
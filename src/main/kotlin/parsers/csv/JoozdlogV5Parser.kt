package nl.joozd.rosterparser.parsers.csv

import nl.joozd.rosterparser.ParsedFlight
import nl.joozd.rosterparser.ParsedRoster
import nl.joozd.rosterparser.ParsedSimulatorDuty
import nl.joozd.rosterparser.ParsingException
import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.factories.CSVParserConstructor
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.minutes

/**
 * Parses a Joozdlog V5 CSV file
 * A Joozdlog V5 CSV File is the backup file created by Joozdlog.
 */
class JoozdlogV5Parser (private val lines: List<String>) : CSVParser() {
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
                        flightMapToSim(flightMap)
                    else
                        flightMapToFlight(flightMap)
                )
            } catch (e: Exception) {
                throw ParsingException("Cannot parse line $line", e)
            }
        }
        timeZone = ZoneOffset.UTC
        flightsArePlanned = false
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
            numberOfAutolands = flightMap["autoLand"]?.toInt(),

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
            isPF = flightMap["isPF"] == "true",
            remarks = flightMap["remarks"]!!,
            signatureSVG = flightMap["signatureSVG"]!!
        )

    }

    private fun getOtherNames(flightMap: Map<String, String>) = flightMap["name2"]!!.split("|")


    companion object : CSVParserConstructor {
        override fun createIfAble(csvLines: List<String>): JoozdlogV5Parser? =
            if (canCreateFromLines(csvLines)) JoozdlogV5Parser(csvLines)
            else null

        private fun canCreateFromLines(lines: List<String>): Boolean =
            lines.firstOrNull() == FINGERPRINT

        private const val FINGERPRINT =
            "flightID;Origin;dest;timeOut;timeIn;correctedTotalTime;multiPilotTime;nightTime;ifrTime;simTime;aircraftType;registration;name;name2;takeOffDay;takeOffNight;landingDay;landingNight;autoLand;flightNumber;remarks;isPIC;isPICUS;isCoPilot;isDual;isInstructor;isSim;isPF;isPlanned;autoFill;augmentedCrew;signatureSVG"
    }

    /**
     * Snapshot copied from JoozdLog.
     * This version works for the V5 Joozdlog CSVs and decodes the "augmented crew" information.
     * Ripped out unused parts.
     */
    private data class AugmentedCrew(
        val isFixedTime: Boolean = false,
        val size: Int = 2, // this gets ignored for calculations if isFixedTime is true
        val takeoff: Boolean = true,
        val landing: Boolean = true,
        val times: Int = 0,
        val isUndefined: Boolean = false)
    {
        companion object {
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
                return this.and(1.shl(n)) > 0 // it's more than 0 so the set bit is 1, whatever bit it is
            }
        }
    }
}
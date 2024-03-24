package nl.joozd.rosterparser.parsers.csv

import nl.joozd.rosterparser.ParsedRoster
import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.factories.CSVParserConstructor

class JoozdlogV5Parser private constructor(lines: List<String>) : CSVParser() {
    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws nl.joozd.rosterparser.ParsingException when the data used to construct this parser cannot be parsed after all
     */
    override fun getRoster(): ParsedRoster = ParsedRoster.build {
        TODO()
    }


    companion object : CSVParserConstructor {
        override fun createIfAble(csvLines: List<String>): CSVParser? =
            if (canCreateFromLines(csvLines)) JoozdlogV5Parser(csvLines)
            else null

        private fun canCreateFromLines(lines: List<String>): Boolean =
            lines.first() == FINGERPRINT

        private const val FINGERPRINT = "flightID;Origin;dest;timeOut;timeIn;correctedTotalTime;multiPilotTime;nightTime;ifrTime;simTime;aircraftType;registration;name;name2;takeOffDay;takeOffNight;landingDay;landingNight;autoLand;flightNumber;remarks;isPIC;isPICUS;isCoPilot;isDual;isInstructor;isSim;isPF;isPlanned;autoFill;augmentedCrew;signatureSVG"
    }
}
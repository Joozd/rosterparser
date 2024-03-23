package nl.joozd.rosterparser.parsers.csv

import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.factories.CSVParserConstructor

class JoozdlogV5Parser private constructor(lines: List<String>): CSVParser(){
    companion object: CSVParserConstructor {
        override fun createIfAble(csvLines: List<String>): CSVParser? {
            if(canCreateFromLines(csvLines))
                return JoozdlogV5Parser(csvLines)
            else return null
        }

        private fun canCreateFromLines(lines: List<String>): Boolean =
            TODO()
    }
}
package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.CSVParser

interface CSVParserConstructor {
    /**
     *  If [csvLines] can be used to create this oject, create it. Else, return null.
     */
    fun createIfAble(csvLines: List<String>): CSVParser?
}
package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.CSVParser

/**
 * Implement this interface with any CSV Parser's companion object,
 * so it can be created by [CSVParserFactory].
 */
interface CSVParserConstructor {
    /**
     *  If [csvLines] can be used to create this object, create it. Else, return null.
     */
    fun createIfAble(csvLines: List<String>): CSVParser?
}
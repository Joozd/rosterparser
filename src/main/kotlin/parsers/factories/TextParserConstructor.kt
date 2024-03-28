package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.TextParser

/**
 * Implement this interface with any Text Parser's companion object,
 * so it can be created by [TextParserFactory].
 */
interface TextParserConstructor {
    /**
     *  If [lines] can be used to create this object, create it. Else, return null.
     */
    fun createIfAble(lines: List<String>): TextParser?

}
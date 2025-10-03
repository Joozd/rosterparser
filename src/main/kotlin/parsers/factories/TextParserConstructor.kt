package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.TextParser
import nl.joozd.rosterparser.parsers.progress.Progress

/**
 * Implement this interface with any Text Parser's companion object,
 * so it can be created by [TextParserFactory].
 */
interface TextParserConstructor {
    /**
     *  If [text] can be used to create this object, create it. Else, return null.
     */
    fun createIfAble(text: String, onProgress: (Progress) -> Unit = {}): TextParser?

}
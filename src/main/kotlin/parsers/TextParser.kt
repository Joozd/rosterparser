package nl.joozd.rosterparser.parsers

import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.factories.TextParserFactory
import java.io.InputStream

/**
 * TextParsers must be registered in [nl.joozd.rosterparser.parsers.factories.ParsersRegistry]
 * in order for them to be used
 */
abstract class TextParser: RosterParser() {
    companion object{
        /**
         * Create a new CSV Parser with the data in [inputStream]
         * @param inputStream an InputStream containing text data
         *
         * @return a TextParser object, or null if unable to create one
         *
         * Contains blocking IO
         */
        internal fun ofInputStream(inputStream: InputStream): TextParser?=
            TextParserFactory.getTextParser(inputStream)
    }
}
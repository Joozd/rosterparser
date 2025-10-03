package nl.joozd.rosterparser.parsers

import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.factories.TextParserFactory
import nl.joozd.rosterparser.parsers.progress.Progress
import java.io.InputStream

/**
 * TextParsers must be registered in [nl.joozd.rosterparser.parsers.factories.ParsersRegistry]
 * in order for them to be used
 */
abstract class TextParser(onProgress: (Progress) -> Unit): RosterParser(onProgress = onProgress) {
    companion object{
        /**
         * Create a new Text Parser with the data in [inputStream]
         * @param inputStream an InputStream containing text data
         *
         * @return a TextParser object, or null if unable to create one
         *
         * Contains blocking IO
         */
        internal fun ofInputStream(inputStream: InputStream, onProgress: (Progress) -> Unit = {}): TextParser?=
            TextParserFactory.getTextParser(inputStream, onProgress)
    }
}
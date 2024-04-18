package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.TextParser
import java.io.InputStream

internal object TextParserFactory {
    /**
     * Creates a CSVParser if able, or null if not.
     *
     * @param inputStream An InputStream with CSV Data
     *
     * @return a CSVParser object, or null if no suitable creator can be found.
     */
    fun getTextParser(inputStream: InputStream): TextParser? {
        val text = inputStream.bufferedReader().readText() // closing the stream is responsibility of whoever created it
        return ParsersRegistry.textParsers.firstNotNullOfOrNull { creator ->
            creator.createIfAble(text)
        }
    }
}
package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.services.readLines
import java.io.InputStream

internal object CSVParserFactory {
    /**
     * Creates a CSVParser if able, or null if not.
     *
     * @param inputStream An InputStream with CSV Data
     *
     * @return a CSVParser object, or null if no suitable creator can be found.
     */
    fun getCsvParser(inputStream: InputStream): CSVParser? {
        val lines = readLines(inputStream) // closing the stream is responsibility of whoever created it
        return ParsersRegistry.csvParsers.firstNotNullOfOrNull { creator ->
            creator.createIfAble(lines)
        }
    }
}
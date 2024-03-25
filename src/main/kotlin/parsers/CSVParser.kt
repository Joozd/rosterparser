package nl.joozd.rosterparser.parsers

import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.factories.CSVParserFactory
import java.io.InputStream

/**
 * CSVParsers must be registered in [nl.joozd.rosterparser.parsers.factories.ParsersRegistry]
 * in order for them to be used
 */
abstract class CSVParser: RosterParser() {
    companion object{
        /**
         * Create a new CSV Parser with the data in [inputStream]
         * @param inputStream an InputStream containing CSV data
         *
         * @return a CSVParser object, or null if unable to create one
         *
         * Contains blocking IO
         */
        internal fun ofInputStream(inputStream: InputStream): CSVParser? =
            CSVParserFactory.getCsvParser(inputStream)
    }
}
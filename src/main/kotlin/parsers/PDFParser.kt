package nl.joozd.rosterparser.parsers

import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.factories.PDFParserFactory
import nl.joozd.rosterparser.parsers.progress.Progress
import java.io.InputStream

/**
 * PDFParsers must be registered in [nl.joozd.rosterparser.parsers.factories.ParsersRegistry]
 * in order for them to be used
 */
abstract class PDFParser(onProgress: (Progress) -> Unit): RosterParser(onProgress = onProgress) {
    companion object{
        /**
         * Create a new CSV Parser with the data in [inputStream]
         * @param inputStream an InputStream containing PDF data
         *
         * @return a PDFParser object, or null if unable to create one
         *
         * Contains blocking IO
         */
        internal fun ofInputStream(inputStream: InputStream, onProgress: (Progress) -> Unit = {}): PDFParser? =
            PDFParserFactory.getPdfParser(inputStream, onProgress)
    }
}
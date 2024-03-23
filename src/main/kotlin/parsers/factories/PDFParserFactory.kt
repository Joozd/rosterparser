package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.PDFParser
import java.io.InputStream

internal object PDFParserFactory {
    /**
     * Creates a CSVParser if able, or null if not.
     *
     * @param inputStream An InputStream with CSV Data
     *
     * @return a CSVParser object, or null if no suitable creator can be found.
     */
    fun getPdfParser(inputStream: InputStream): PDFParser? {
        val lines = inputStream.bufferedReader().readLines() // closing the stream is responsibility of whoever created it
        return ParsersRegistry.pdfParsers.firstNotNullOfOrNull { creator ->
            creator.createIfAble(lines)
        }
    }
}
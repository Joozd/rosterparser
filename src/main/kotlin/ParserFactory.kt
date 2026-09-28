package nl.joozd.rosterparser

import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.TextParser
import nl.joozd.rosterparser.parsers.progress.Progress
import java.io.InputStream

internal object ParserFactory {
    // Supported MimeTypes


    /**
     * Get a parser for an inputStream with type [mimeType]
     * @param onProgress The action to take when progress is reported.
     *  Progress reports are expected maybe a few times for inputStream reading (if it can be determined ahead of time)
     *  and a maximum of once per parsed duty
     */
    fun getParserForMimeType(mimeType: String, inputStream: InputStream, onProgress: (Progress) -> Unit): RosterParser =
        when (mimeType) {
            MimeTypes.CSV, MimeTypes.CSV_LONG -> CSVParser.ofInputStream(inputStream, onProgress)
            MimeTypes.PDF -> PDFParser.ofInputStream(inputStream, onProgress)
            MimeTypes.TEXT -> TextParser.ofInputStream(inputStream, onProgress)
            else -> throw IllegalArgumentException("MIME type $mimeType not supported for creating a RosterParser object")
        } ?: throw IllegalArgumentException("Unable to create parser from InputStream with mimetype $mimeType")
}
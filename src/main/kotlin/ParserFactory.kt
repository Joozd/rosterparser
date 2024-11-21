package nl.joozd.rosterparser

import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.TextParser
import java.io.InputStream

internal object ParserFactory {
    // Supported MimeTypes


    fun getParserForMimeType(mimeType: String, inputStream: InputStream): RosterParser =
        when (mimeType) {
            MimeTypes.CSV -> CSVParser.ofInputStream(inputStream)
            MimeTypes.PDF -> PDFParser.ofInputStream(inputStream)
            MimeTypes.TEXT -> TextParser.ofInputStream(inputStream)
            else -> throw IllegalArgumentException("MIME type $mimeType not supported for creating a RosterParser object")
        } ?: throw IllegalArgumentException("Unable to create parser from InputStream with mimetype $mimeType")
}
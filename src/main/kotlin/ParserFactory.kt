package nl.joozd.rosterparser

import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.TextParser
import java.io.InputStream

internal object ParserFactory {
    // Supported MimeTypes
    private const val MIME_TYPE_CSV = "text/csv"            // parsed by CSVParser
    private const val MIME_TYPE_PDF = "application/pdf"     // parsed by PDFParser
    private const val MIME_TYPE_TEXT = "text/plain"         // parsed by TextParser

    fun getParserForMimeType(mimeType: String, inputStream: InputStream): RosterParser =
        when (mimeType) {
            MIME_TYPE_CSV -> CSVParser.ofInputStream(inputStream)
            MIME_TYPE_PDF -> PDFParser.ofInputStream(inputStream)
            MIME_TYPE_TEXT -> TextParser.ofInputStream(inputStream)
            else -> throw IllegalArgumentException("MIME type $mimeType not supported for creating a RosterParser object")
        }
}
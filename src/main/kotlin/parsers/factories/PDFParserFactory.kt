package nl.joozd.rosterparser.parsers.factories

import com.itextpdf.text.exceptions.InvalidPdfException
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.progress.Progress
import nl.joozd.rosterparser.parsers.progress.ReadingFile
import java.io.InputStream

internal object PDFParserFactory {
    /**
     * Creates a CSVParser if able, or null if not.
     *
     * @param inputStream An InputStream with CSV Data
     * @param onProgress The action to take when progress is reported.
     *
     * @return a CSVParser object, or null if no suitable creator can be found.
     */
    fun getPdfParser(inputStream: InputStream, onProgress: (Progress) -> Unit = {}): PDFParser? {
        try {
            val reader = PdfReader(inputStream)
            val numberOfPages = reader.numberOfPages

            // onProgress will call once per page
            onProgress(ReadingFile(0f))

            val lines = (1..numberOfPages).map { page ->
                PdfTextExtractor.getTextFromPage(reader, page, SimpleTextExtractionStrategy()).lines()
                    .also { onProgress(ReadingFile(numberOfPages.toFloat() / page)) }
            }.flatten()

            // If this is too slow, an async solution might bring some better performance
            return ParsersRegistry.pdfParsers.firstNotNullOfOrNull { creator ->
                creator.createIfAble(lines, reader, onProgress)
            }.also{
                if (it == null)
                    onProgress(Progress.FAILED)
            }
        } catch (e: InvalidPdfException){
            onProgress(Progress.FAILED)
            throw IllegalArgumentException("PDFParserFactory could not read PDF, probably bad data", e)
        }
    }
}
package nl.joozd.rosterparser.parsers.factories

import com.itextpdf.text.exceptions.InvalidPdfException
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
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
        try {
            val reader = PdfReader(inputStream)
            val lines = (1..reader.numberOfPages).map { page ->
                PdfTextExtractor.getTextFromPage(reader, page, SimpleTextExtractionStrategy()).lines()
            }.flatten()

            // If this is too slow, an async solution might bring some better performance
            return ParsersRegistry.pdfParsers.firstNotNullOfOrNull { creator ->
                creator.createIfAble(lines, reader)
            }
        } catch (e: InvalidPdfException){
            throw IllegalArgumentException("PDFParserFactory could not read PDF, probably bad data")
        }
    }
}

/*
/**
 * Gets a random not-null value from a collection, or null if none found.
 * Returns the first one it finds, but due to parallel processing there are no guarantees which one that is,
 * or even if it is the same one when executed multiple times in a row.
 * NOTE:UNTESTED
 */
private fun <TYPE_IN, TYPE_OUT> Collection<TYPE_IN>.firstNotNullAsync(block: (TYPE_IN) -> TYPE_OUT?): TYPE_OUT? {
    val numThreads = maxOf(2, Runtime.getRuntime().availableProcessors() - 1)
    val pools = this.chunked(this.size / numThreads + 1)
    var found: TYPE_OUT? = null

    runBlocking {
        pools.map { pool ->
            async(Dispatchers.Default) {
                for (item in pool) {
                    if (found != null) return@async // Check if found and exit
                    val result = block(item)
                    if (result != null) {
                        found = result
                        return@async // Exit after finding a result
                    }
                }
            }
        }.forEach { it.await() } // Wait for all async operations to complete
    }

    return found
}
*/
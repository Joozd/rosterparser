package nl.joozd.rosterparser.parsers.factories

import com.itextpdf.text.pdf.PdfReader
import nl.joozd.rosterparser.parsers.PDFParser

/**
 * Implement this interface with any PDF Parser's companion object,
 * so it can be created by [PDFParserFactory].
 * A Parser should be able to determine if it can parse data from text extracted with
 * `PdfTextExtractor.getTextFromPage(reader, page, SimpleTextExtractionStrategy()).lines()` (all pages concatenated).
 *
 * If unable, it can use the PdfReader, but that does have performance implications so use that only if necessary.
 *
 * If multiple parsers state they can parse the provided data, it is undefined which one will get it.
 * If it turns out it cannot parse it after all, a second one will not be tried.
 */
interface PDFParserConstructor {
    /**
     * If [pdfLines] can be used to create this object, create it. Else, return null.
     * As a fallback option, [pdfReader] can be used for creation, but it should only be a last resort because
     * of performance concerns (parsing a PDF is expensive and there could be a lot of parsers trying to be created).
     * @param pdfLines The lines in this document as received from
     *  PdfTextExtractor.getTextFromPage(reader, page, SimpleTextExtractionStrategy()).lines()`
     *  (all pages concatenated). This is the prefered data to determine
     * @param pdfReader PdfReader object containing the PDF roster to be parsed, if able.
     *  Not recommended to use this for checking if the parser can be created due to performance reasons.
     */
    fun createIfAble(pdfLines: List<String>, pdfReader: PdfReader): PDFParser?
}
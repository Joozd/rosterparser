package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.PDFParser

/**
 * Implement this interface with any PDF Parser's companion object,
 * so it can be created by [PDFParserFactory].
 */
interface PDFParserConstructor {
    /**
     *  If [pdfLines] can be used to create this object, create it. Else, return null.
     */
    fun createIfAble(pdfLines: List<String>): PDFParser?

}
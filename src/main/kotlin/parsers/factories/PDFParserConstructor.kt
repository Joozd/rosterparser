package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.PDFParser

interface PDFParserConstructor {
    /**
     *  If [pdfLines] can be used to create this object, create it. Else, return null.
     */
    fun createIfAble(pdfLines: List<String>): PDFParser?

}
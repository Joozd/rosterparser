package parsers.pdf

import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.factories.CSVParserConstructor
import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import org.junit.jupiter.api.Assertions
import parsers.ParserSubclassTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Parent class for standardized CSV Parser Testing.
 * Tests for correct construction through [parserConstructor] and handles data loading from [testResourceName]
 *
 * @property testResourceName Name of the resource in test/resources that holds the test data
 * @property parserConstructor the [CSVParserConstructor] object that constructs the parser to be tested
 */
abstract class PdfParserSubclassTest : ParserSubclassTest() {
    abstract val parserConstructor: PDFParserConstructor
    abstract val expectedParserType: Class<out PDFParser>

    protected val parser by lazy { createParser() }
    private fun createParser() = getResourceInputStream().use {
        val reader = PdfReader(it)
        val lines = (1..reader.numberOfPages).map { page ->
            PdfTextExtractor.getTextFromPage(reader, page, SimpleTextExtractionStrategy()).lines()
        }.flatten()
        parserConstructor.createIfAble(lines, reader)
    }


    @Test
    fun testConstructsCorrectly() {
        // Check bad data handling:
        val pdfBadDataFile = File(this::class.java.classLoader.getResource("pdf_bad_data.pdf")!!.toURI())
        Assertions.assertNull(
            parserConstructor.createIfAble(listOf("Bad Data"), PdfReader(pdfBadDataFile.inputStream())),
            "Parser should return null for bad data"
        )

        // Check if parser can be made from the data and is of the correct type
        Assertions.assertNotNull(parser, "Parser should not be null")

        // Additional type check if needed, replace ExpectedParserType with the expected type
        assertIs<RosterParser>(parser, "Parser is not of the expected type")

        val constructedParser = getResourceInputStream().use { PDFParser.ofInputStream(it) }!!
        assert(expectedParserType.isInstance(constructedParser)) { "Expected parser of type ${expectedParserType.simpleName} but got ${constructedParser::class.simpleName}" }
    }
}
package parsers

import nl.joozd.rosterparser.parsers.pdf.KlcBriefingSheetParser
import nl.joozd.rosterparser.parsers.factories.PDFParserFactory
import java.io.File
import kotlin.test.*

class PDFParserFactoryTest {
    private val klcBriefingSheetTestFile =
        File(this::class.java.classLoader.getResource("klcbriefingsheettest.pdf")!!.toURI())
    private val pdfBadDataFile = File(this::class.java.classLoader.getResource("pdf_bad_data.pdf")!!.toURI())

    @Test
    fun testKlcBriefingSheetParserCreation() {
        val parser = klcBriefingSheetTestFile.inputStream().use { PDFParserFactory.getPdfParser(it) }
        // Check if parser can be made from the data and is of the correct type
        assertNotNull(parser)
        assertIs<KlcBriefingSheetParser>(parser)
    }

    @Test
    fun testPDFParsersAllReturnNullOnInvalidData() {
        // Assuming "bad PDF, not for parsing" means the structure is good but content is not suitable
        // If PDFParserFactory should return null for unsuitable content, test with actual bad data PDF
        val parserFromBadData = pdfBadDataFile.inputStream().use { PDFParserFactory.getPdfParser(it) }
        assertNull(parserFromBadData)

        // Testing with empty and nonsensical byte streams to simulate bad data at a more fundamental level
        val empty = "".byteInputStream()
        val badData = "BAD DATA".byteInputStream()
        assertFailsWith<IllegalArgumentException> { empty.use { PDFParserFactory.getPdfParser(it) } }
        assertFailsWith<IllegalArgumentException> { badData.use { PDFParserFactory.getPdfParser(it) } }
    }
}

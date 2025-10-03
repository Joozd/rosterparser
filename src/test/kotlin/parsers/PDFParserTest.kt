package parsers

import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.PDFParser
import nl.joozd.rosterparser.parsers.progress.Progress
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PDFParserTest {
    private val pdfTestFile = File(this::class.java.classLoader.getResource("klcbriefingsheettest.pdf")!!.toURI())
    private val mimeType = java.nio.file.Files.probeContentType(pdfTestFile.toPath())

    @Test
    fun testPdfParserCreation(){
        var currentProgress: Progress? = null
        val parser = pdfTestFile.inputStream().use{
            RosterParser.ofInputStream(it, mimeType){ progress ->
                println(progress)
                currentProgress = progress
            }
        }
        assertEquals(Progress.CREATED, currentProgress)
        assertIs<PDFParser>(parser)
    }
}
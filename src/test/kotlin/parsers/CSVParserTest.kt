package parsers

import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.CSVParser
import java.io.File
import kotlin.test.Test
import kotlin.test.assertIs

class CSVParserTest {
    private val csvTestFile = File(this::class.java.classLoader.getResource("joozdlogv5test.csv")!!.toURI())
    private val mimeTypeCSV = "text/csv"

    @Test
    fun testCsvParserCreation(){
        val parser = csvTestFile.inputStream().use{
            RosterParser.ofInputStream(it, mimeTypeCSV)
        }
        assertIs<CSVParser>(parser)
    }
}
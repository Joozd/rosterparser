package parsers

import nl.joozd.rosterparser.parsers.csv.JoozdlogV5Parser
import nl.joozd.rosterparser.parsers.factories.CSVParserFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CSVParserFactoryTest {
    private val joozdlogV5TestFile = File(this::class.java.classLoader.getResource("joozdlogv5test.csv")!!.toURI())

    fun testJoozdlogV5ParserCreation(){
        val parser = joozdlogV5TestFile.inputStream().use { CSVParserFactory.getCsvParser(it) }
        //check if parser can be made from the data and is of the correct type
        assertNotNull(parser)
        assertIs<JoozdlogV5Parser>(parser)
    }
    @Test
    fun testCSVParsersAllReturnNullOnInvalidData(){
        val empty = "".byteInputStream()
        val badData = "BAD BATA".byteInputStream()
        val moreBadData = "BAD BATA\nMORE BAD DATA\nEVEN MORE BAD DATA".byteInputStream()
        assertNull(empty.use { CSVParserFactory.getCsvParser(it) })
        assertNull(badData.use { CSVParserFactory.getCsvParser(it) })
        assertNull(moreBadData.use { CSVParserFactory.getCsvParser(it) })
    }
}
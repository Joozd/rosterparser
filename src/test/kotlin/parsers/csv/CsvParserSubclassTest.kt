package parsers.csv

import nl.joozd.rosterparser.RosterParser
import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.factories.CSVParserConstructor
import nl.joozd.rosterparser.services.text.readLines
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
abstract class CsvParserSubclassTest: ParserSubclassTest() {
    abstract val parserConstructor: CSVParserConstructor
    abstract val expectedParserType: Class<out CSVParser>

    protected val parser by lazy { createParser() }
    private fun createParser() = File(this::class.java.classLoader.getResource(testResourceName)!!.toURI())
        .inputStream()
        .use{
            parserConstructor.createIfAble(readLines(it))
        }

    @Test
    fun testConstructsCorrectly() {
        // Check bad data handling:
        assertNull(parserConstructor.createIfAble(listOf("Bad Data")), "Parser should return null for bad data")

        // Check if parser can be made from the data and is of the correct type
        assertNotNull(parser, "Parser should not be null")

        // Additional type check if needed, replace ExpectedParserType with the expected type
        assertIs<RosterParser>(parser, "Parser is not of the expected type")

        val constructedParser = getResourceInputStream().use { CSVParser.ofInputStream(it) }!!
        assert(expectedParserType.isInstance(constructedParser)) { "Expected parser of type ${expectedParserType.simpleName} but got ${constructedParser::class.simpleName}"}
    }
}
package parsers.pdf

import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import nl.joozd.rosterparser.parsers.pdf.KlmMonthlyParser
import org.junit.jupiter.api.Assertions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KlmMonthlyParserSimAllInstructionTest : PdfParserSubclassTest() {
    override val testResourceName: String = "KLM_2_flights_4_sims_instruction.pdf"
    override val parserConstructor: PDFParserConstructor = KlmMonthlyParser
    override val expectedParserType = KlmMonthlyParser::class.java

    @Test
    fun testParser() {
        Assertions.assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<KlmMonthlyParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(2, parsedRoster.flights.size, "Expected 2 flights in the parsed roster")
        assertEquals(4, parsedRoster.simulatorDuties.size, "Expected 4 sims in the parsed roster, got\n${parsedRoster.simulatorDuties.joinToString("\n")}")
//
        assert(parsedRoster.simulatorDuties.all { it.instructionGiven == true })
    }
}
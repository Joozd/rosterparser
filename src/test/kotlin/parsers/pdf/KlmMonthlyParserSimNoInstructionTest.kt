package parsers.pdf

import nl.joozd.rosterparser.parsers.factories.PDFParserConstructor
import nl.joozd.rosterparser.parsers.pdf.KlmMonthlyParser
import org.junit.jupiter.api.Assertions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Only tests correct parsing of sims and number of duties.
 * More complete tests are in [KlmMonthlyParsersTest]
 */
class KlmMonthlyParserSimNoInstructionTest : PdfParserSubclassTest() {
    override val testResourceName: String = "KLM_6_flights_5_sims_no_instruction.pdf"
    override val parserConstructor: PDFParserConstructor = KlmMonthlyParser
    override val expectedParserType = KlmMonthlyParser::class.java

    @Test
    fun testParser() {
        Assertions.assertNotNull(parser, "Parser is null, cannot be constructed from test data")
        assertIs<KlmMonthlyParser>(parser, "Parser is not an instance of JoozdlogV5Parser")

        val parsedRoster = parser!!.getRoster()

        assertEquals(6, parsedRoster.flights.size, "Expected 6 flights in the parsed roster")
        assertEquals(5, parsedRoster.simulatorDuties.size, "Expected 5 sims in the parsed roster")
//
        assert(parsedRoster.simulatorDuties.all { it.instructionGiven == false })
    }
}
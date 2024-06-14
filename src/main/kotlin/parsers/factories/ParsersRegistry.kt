package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.csv.JoozdlogV5Parser
import nl.joozd.rosterparser.parsers.csv.MccPilotLogParser
import nl.joozd.rosterparser.parsers.pdf.KlcBriefingSheetParser
import nl.joozd.rosterparser.parsers.pdf.KlcMonthlyParser
import nl.joozd.rosterparser.parsers.pdf.KlcRosterParser
import nl.joozd.rosterparser.parsers.pdf.KlmMonthlyParser
import nl.joozd.rosterparser.parsers.text.LogtenProOldParser
import nl.joozd.rosterparser.parsers.text.TestDataParser

internal object ParsersRegistry {
    /**
     * A list of all possible CSV Parser constructor objects.
     * @see CSVParserConstructor
     */
    val csvParsers: List<CSVParserConstructor> = listOf(
        JoozdlogV5Parser,
        MccPilotLogParser
    )

    /**
     * A list of all possible PDF Parser constructor objects.
     * @see PDFParserConstructor
     */
    val pdfParsers: List<PDFParserConstructor> = listOf(
        KlcBriefingSheetParser,
        KlmMonthlyParser,
        KlcMonthlyParser,
        KlcRosterParser
    )

    /**
     * A list of all possible Text Parser constructor objects.
     * @see TextParserConstructor
     */
    val textParsers: List<TextParserConstructor> = listOf(
        LogtenProOldParser,
        TestDataParser
    )
}
package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.csv.JoozdlogV5Parser
import nl.joozd.rosterparser.parsers.csv.MccPilotLogParser

internal object ParsersRegistry {
    val csvParsers: List<CSVParserConstructor> = listOf(
        JoozdlogV5Parser,
        MccPilotLogParser
    )

    val pdfParsers: List<PDFParserConstructor> = listOf(

    )

    val textParsers: List<TextParserConstructor> = listOf(

    )
}
package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.csv.JoozdlogV5Parser

internal object ParsersRegistry {
    val csvParsers: List<CSVParserConstructor> = listOf(
        JoozdlogV5Parser
    )

    val pdfParsers: List<PDFParserConstructor> = listOf(

    )

    val textParsers: List<TextParserConstructor> = listOf(

    )
}
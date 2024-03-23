package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.csv.JoozdlogV5Parser

internal object ParsersRegistry {
    val csvParsers: List<CSVParserConstructor> = listOf(
        JoozdlogV5Parser
    )
}
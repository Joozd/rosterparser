package nl.joozd.rosterparser.parsers

import nl.joozd.rosterparser.RosterParser
import java.io.InputStream

abstract class PDFParser: RosterParser() {
    companion object{
        internal fun ofInputStream(inputStream: InputStream): RosterParser {
            TODO()
        }
    }
}
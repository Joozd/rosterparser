package nl.joozd.rosterparser.parsers

import nl.joozd.rosterparser.RosterParser
import java.io.InputStream

class TextParser: RosterParser() {
    companion object{
        internal fun ofInputStream(inputStream: InputStream): RosterParser {
            TODO()
        }
    }
}
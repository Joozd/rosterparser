@file:Suppress("MemberVisibilityCanBePrivate")

package nl.joozd.rosterparser

import java.io.InputStream

/**
 * A Rosterparser can parse a Roster (of overview if after the fact) for a flight Schedule to a ParsedRoster.
 */
abstract class RosterParser {
    companion object {
        /**
         * Creates a RosterParser object from [inputStream]
         *
         * @param inputStream The InputStream with the data to parse
         * @param mimeType The MimeType of the data in [inputStream]
         * @return A RosterParser object
         * Contains Blocking IO
         */
        fun ofInputStream(inputStream: InputStream, mimeType: String): RosterParser =
            ParserFactory.getParserForMimeType(mimeType, inputStream)
    }
}
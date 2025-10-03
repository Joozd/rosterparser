@file:Suppress("MemberVisibilityCanBePrivate")

package nl.joozd.rosterparser

import nl.joozd.rosterparser.parsers.progress.Progress
import java.io.InputStream

/**
 * A RosterParser can parse a Roster (of overview if after the fact) for a flight Schedule to a ParsedRoster.
 * @param onProgress a listener for progress updates. This might update quite often (i.e. multiple times per line)
 */
abstract class RosterParser(protected val onProgress: (Progress) -> Unit = {}) {
    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws ParsingException when the data used to construct this parser cannot be parsed after all
     */
    abstract fun getRoster(): ParsedRoster

    companion object {
        /**
         * Creates a RosterParser object from [inputStream]
         *
         * @param inputStream The InputStream with the data to parse
         * @param mimeType The MimeType of the data in [inputStream]
         * @return A RosterParser object.
         * @throws IllegalArgumentException if unable to create a RosterParser with the provided InputStream/mimetype combination.
         *
         * Contains Blocking IO
         */
        fun ofInputStream(inputStream: InputStream, mimeType: String, onProgress: (Progress) -> Unit = {}): RosterParser =
            ParserFactory.getParserForMimeType(mimeType, inputStream, onProgress)


        /**
         * Generates a [ParsedRoster] from [inputStream]
         *
         * @param inputStream The InputStream with the data to parse
         * @param mimeType The MimeType of the data in [inputStream]
         * @return A [ParsedRoster] object
         * @throws[IllegalArgumentException] if unable to create a RosterParser with the provided InputStream/mimetype combination.
         * @throws[ParsingException] if the created parser ran into a problem while parsing the data (bad data or unexpected format variant)
         */
        fun getRoster(inputStream: InputStream, mimeType: String, onProgress: (Progress) -> Unit = {}) =
            ofInputStream(inputStream, mimeType, onProgress).getRoster()
    }
}
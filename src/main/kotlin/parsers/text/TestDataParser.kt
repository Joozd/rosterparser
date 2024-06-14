package nl.joozd.rosterparser.parsers.text

import nl.joozd.rosterparser.ParsedRoster
import nl.joozd.rosterparser.ParsingException
import nl.joozd.rosterparser.parsers.TextParser
import nl.joozd.rosterparser.parsers.factories.TextParserConstructor
import nl.joozd.rosterparser.testing.RosterParserTestData
import nl.joozd.rosterparser.testing.buildSampleRoster

/**
 * This parses only the RosterParser TestData data, and returns a sample roster, using [buildSampleRoster]
 */
class TestDataParser: TextParser() {
    /**
     * creates a [ParsedRoster] from the data found in the InputStream used to create this RosterParser.
     *
     * @return a [ParsedRoster] object
     *
     * @throws ParsingException when the data used to construct this parser cannot be parsed after all
     */
    override fun getRoster(): ParsedRoster =
        buildSampleRoster() // from


    companion object : TextParserConstructor {
        /**
         *  If [text] can be used to create this object, create it. Else, return null.
         */
        override fun createIfAble(text: String): TextParser? =
            if (text == RosterParserTestData.dataString)
                TestDataParser()
            else null
    }
}
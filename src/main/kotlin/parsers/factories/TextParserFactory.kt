package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.TextParser
import nl.joozd.rosterparser.parsers.progress.Progress
import nl.joozd.rosterparser.parsers.progress.ReadingFile
import nl.joozd.rosterparser.services.text.readText
import java.io.InputStream

internal object TextParserFactory {
    /**
     * Creates a CSVParser if able, or null if not.
     *
     * @param inputStream An InputStream with CSV Data
     *
     * @return a CSVParser object, or null if no suitable creator can be found.
     */
    fun getTextParser(inputStream: InputStream, onProgress: (Progress) -> Unit): TextParser? {

        onProgress(ReadingFile(0f))

        val text = readText(inputStream) // closing the stream is responsibility of whoever created it

        onProgress(ReadingFile(1f))

        return ParsersRegistry.textParsers.firstNotNullOfOrNull { creator ->
            creator.createIfAble(text, onProgress)
        }.also{
            if (it == null)
                onProgress(Progress.FAILED)
        }
    }
}
package nl.joozd.rosterparser.parsers.factories

import nl.joozd.rosterparser.parsers.CSVParser
import nl.joozd.rosterparser.parsers.progress.Progress
import nl.joozd.rosterparser.parsers.progress.ReadingFile
import nl.joozd.rosterparser.services.text.readLines
import java.io.InputStream

internal object CSVParserFactory {
    /**
     * Creates a CSVParser if able, or null if not.
     *
     * @param inputStream An InputStream with CSV Data
     * @param onProgress The action to take when progress is reported.
     *
     * @return a CSVParser object, or null if no suitable creator can be found.
     */
    fun getCsvParser(inputStream: InputStream, onProgress: (Progress) -> Unit = {}): CSVParser? {
        // Cannot determine how many lines to read, because we have to read tghem to know how many there are.
        onProgress(ReadingFile(0f))

        val lines = readLines(inputStream) // closing the stream is responsibility of whoever created it

        onProgress(ReadingFile(1f))

        return ParsersRegistry.csvParsers.firstNotNullOfOrNull { creator ->
            creator.createIfAble(lines, onProgress)
        }.also{
            if (it == null)
                onProgress(Progress.FAILED)
        }
    }
}
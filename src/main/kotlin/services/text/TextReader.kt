package nl.joozd.rosterparser.services.text


import com.ibm.icu.text.CharsetDetector
import nl.joozd.rosterparser.utils.extensions.makeReuseable
import java.io.InputStream


/**
 * Reads the text from [inputStream] into a single String using ICU4J's CharsetDetector.
 * This method automatically detects the charset of the input stream and reads it as a string.
 *
 * @param inputStream The input stream from which text is to be read.
 * @return A string containing the text read from the input stream.
 */
fun readText(inputStream: InputStream): String =
    CharsetDetector().apply {
        setText(inputStream.makeReuseable()) // this function needs an InputStream that supports mark/reset which is not guaranteed for all InputStreams
    }
        .detect()
        .string  // Use the best match charset detected to convert the input bytes to string.
        .let{ if (it.startsWith("\uFEFF")) it.substring(1) else it } // strip BOM if present. Might not work for all files? Might need a function that checks string for BOM and removes it if needed?


/**
 * Reads the text from [inputStream] and returns it as a list of lines using ICU4J's CharsetDetector.
 * Automatically detects the charset of the input stream, reads it as a string, and splits into lines.
 *
 * @param inputStream The input stream from which text is to be read.
 * @return A list of strings, each representing a line of text.
 */
fun readLines(inputStream: InputStream): List<String> =
    readText(inputStream).lines()

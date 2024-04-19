package nl.joozd.rosterparser.services.csv


import org.apache.commons.csv.CSVFormat
import java.io.StringReader

/**
 * Reads CSV files.
 * @param csvContent The contents of the CSV file as a single String.
 * @property divider The character used to divide items in this CSV document
 * @property hasHeaders if true, first line contains headers. I false, will use headers from parameter headers
 * @property headers Headers to be used if hasHeaders is false. Ignored if hasHeaders is true.
 * @property quote Character to be used as quote mark. Defaults to "
 * @property nullString String to be used as "null". Defaults to empty String.
 */
class CSVReader(
    csvContent: String,
    private val divider: Char,
    private val hasHeaders: Boolean = true,
    private val headers: List<String>? = null,
    private val quote: Char = '\"',
    private val nullString: String = ""
) {
    /**
     * @see CSVReader
     * Allows CSV to be built from a list of lines (they are provided that way to parsers)
     */
    constructor(csvContent: List<String>,
                divider: Char,
                hasHeaders: Boolean = true,
                headers: List<String>? = null,
                quote: Char = '\"',
                nullString: String = ""
        ): this (csvContent.joinToString("\n"), divider, hasHeaders, headers, quote, nullString)
    // same as csvContent but with guaranteed "\n" as line break,
    private val _csvContent = csvContent.replace("\r\n", "\n")


    /**
     * Make a List of Maps from this CSV data
     * Will throw an IllegalStateException if no headers found
     */
    fun generateRowMaps(): List<Map<String, String>> = buildList {
        val recordsIterator = makeCsvFormatter().parse(StringReader(_csvContent) ).iterator()
        if(!recordsIterator.hasNext())
            if(hasHeaders) throw IllegalStateException("hasHeaders is true but no headers found")
            else return emptyList()
        val keys = if(hasHeaders) recordsIterator.next().toList() else headers ?: throw IllegalStateException("Headers are set null and hasHeaders is false so cannot generate Row maps")
        recordsIterator.forEachRemaining {
            add(keys.zip(it).toMap())
            if(it.size() != keys.size) {
                println("WOWOWOWOW wrong line length!")
                println(last().values.last().toByteArray().toList())
            }
        }
    }

    /**
     * Make a List of Strings for this CSV Data. Includes first row, even if that contains headers.
     */
    fun generateRowLists(): List<List<String>> =
        makeCsvFormatter().parse(StringReader(_csvContent) ).map { it.toList() }

    /**
     * Make a CSVFormatter according to set properties
     */
    private fun makeCsvFormatter(): CSVFormat = CSVFormat.Builder.create()
        .setDelimiter(divider)
        .setQuote(quote)
        .setRecordSeparator("\n") // replace \r\n
        .setIgnoreEmptyLines(true)
        .setSkipHeaderRecord(false) // Always skip header records. We will make maps manually.
        .setTrim(true)
        .setNullString(nullString) // empty fields are null
        .setEscape('\\')
        .build()
}
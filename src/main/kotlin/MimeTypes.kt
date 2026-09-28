package nl.joozd.rosterparser

object MimeTypes {
    const val CSV = "text/csv"            // parsed by CSVParser
    const val CSV_LONG = "text/comma-separated-values"
    const val PDF = "application/pdf"     // parsed by PDFParser
    const val TEXT = "text/plain"         // parsed by TextParser
}
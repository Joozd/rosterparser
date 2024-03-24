package nl.joozd.rosterparser

/**
 * Exception thrown when a parser encounters data that it cannot parse.
 * This might occur if the data is corrupted, in an unexpected format,
 * or otherwise not adhering to the expected structure for parsing.
 *
 * @property message A human-readable message explaining the error. This message
 * is intended to provide further context on why the parsing operation failed.
 * @property cause The underlying cause of the parsing failure, if any. This can be
 * used to attach a lower-level exception that triggered this ParsingException, providing
 * a more detailed stack trace for debugging purposes.
 */
class ParsingException(message: String, cause: Throwable? = null) : Exception(message, cause)
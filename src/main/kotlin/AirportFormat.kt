package nl.joozd.rosterparser

/**
 * The type of format used for airport identifiers.
 * @property ICAO four-letter ICAO identifier, like EHAM or KJFK
 * @property IATA Three-letter IATA identifier, like AMS or JFK
 * @property UNKNOWN The format of the identifier is unknown to the parser. Should only be used if no other option is valid.
 */
enum class AirportFormat {
    ICAO,       // e.g. EHAM
    IATA,       // e.g. AMS
    UNKNOWN     // should not be used unless the parser really doesn't know.
}
package nl.joozd.rosterparser

/**
 * Represents a person's identity information.
 *
 * @property firstName The first name of the person. Optional.
 * @property lastName The last name of the person. Optional.
 * @property middleName The middle name of the person, if any. Optional.
 * @property prefix A prefix to the person's last name, common in various naming conventions, such as "de" in
 * Dutch names ("Jan de Vries"). It is part of the last name used in formal address but is typically omitted
 * in alphabetical sorting. Optional.
 * @property rank Rank of this person. Optional.
 * @property id An arbitrary identifier for the person, which could be a personnel number, passport number,
 * or any other form of identification chosen by users. This field is not strictly formatted, allowing
 * flexibility in the type of identifiers used. Optional.
 */
data class Person(
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val prefix: String? = null,
    val rank: String? = null,
    val id: String? = null
){
    override fun toString() = listOfNotNull(firstName, middleName, prefix, lastName).filter { it.isNotBlank() }.joinToString(" ")

    companion object{
        /**
         * This takes a bit of a guess as to what is first and last name. Use only if no better way available.
         * Assumes names are always FIRST LAST MORE LAST EVEN MORE LAST so "John Baron of Luxembourg and Switzerland" will be correct, "Mary Jane Smith" will not be.
         * getting it back to a string with [toString] will work in both cases.
         * Doesn't do anything with capitalization.
         */
        fun fromString(nameString: String): Person{
            val words = nameString.split(" ").map { it.trim()}.filter { it.isNotBlank()} // split to words, remove empty parts
            return when(words.size){
                0 -> Person()
                1 -> Person(lastName = words.first())
                else -> Person(firstName = words.first(), lastName = words.drop(1).joinToString(" "))
            }
        }
    }
}
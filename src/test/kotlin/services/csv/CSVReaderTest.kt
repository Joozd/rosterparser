package services.csv

import nl.joozd.rosterparser.services.csv.CSVReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CSVReaderTest {

    @Test
    fun `test standard CSV with headers`() {
        val csvContent = """
            name,age,city
            John Doe,30,New York
            Jane Smith,25,Los Angeles
        """.trimIndent()
        val reader = CSVReader(csvContent, ',')
        val rows = reader.generateRowMaps()
        assertEquals(mapOf("name" to "John Doe", "age" to "30", "city" to "New York"), rows[0])
        assertEquals(mapOf("name" to "Jane Smith", "age" to "25", "city" to "Los Angeles"), rows[1])
    }

    @Test
    fun `test CSV with semicolon delimiter and headers`() {
        val csvContent = """
            name;age;city
            John Doe;30;New York
            Jane Smith;25;Los Angeles
        """.trimIndent()
        val reader = CSVReader(csvContent, ';')
        val rows = reader.generateRowMaps()
        assertEquals(mapOf("name" to "John Doe", "age" to "30", "city" to "New York"), rows[0])
    }

    @Test
    fun `test special characters and escape sequences`() {
        val csvContent = "\"name\",\"age\",\"description\"\n" +
                "\"John Doe\",\"30\",\"6'2\"\" tall, likes to play basketball\"\n" +
                "\"Jane Smith\",\"25\",\"Moved to LA from New York\nLoves dogs\"\n"

        val reader = CSVReader(csvContent, ',', quote = '"')
        val rows = reader.generateRowMaps()
        assertEquals("6'2\" tall, likes to play basketball", rows[0]["description"])
        assertEquals("Moved to LA from New York\nLoves dogs", rows[1]["description"])
    }

    @Test
    fun `test CSV without headers using provided headers`() {
        val csvContent = """
            John Doe,30,New York
            Jane Smith,25,Los Angeles
        """.trimIndent()
        val headers = listOf("name", "age", "city")
        val reader = CSVReader(csvContent, ',', hasHeaders = false, headers = headers)
        val rows = reader.generateRowMaps()
        assertEquals(mapOf("name" to "John Doe", "age" to "30", "city" to "New York"), rows[0])
    }

    @Test
    fun `test generate row lists with headers`() {
        val csvContent = """
            name,age,city
            John Doe,30,New York
            Jane Smith,25,Los Angeles
        """.trimIndent()
        val reader = CSVReader(csvContent, ',')
        val rows = reader.generateRowLists()
        assertEquals(listOf("name", "age", "city"), rows[0])
        assertEquals(listOf("John Doe", "30", "New York"), rows[1])
    }

    @Test
    fun `expect error when no headers are provided but generateRowMaps is called`() {
        val csvContent = "John Doe,30,New York"
        val reader = CSVReader(csvContent, ',', hasHeaders = false)
        assertFailsWith<IllegalStateException> {
            reader.generateRowMaps()
        }
    }

    @Test
    fun `test tab-delimited CSV with headers`() {
        val csvContent = "name\tage\tcity\n" +
                "John Doe\t30\tNew York\n" +
                "Jane Smith\t25\tLos Angeles"
        val reader = CSVReader(csvContent, '\t')
        val rows = reader.generateRowMaps()
        assertEquals(mapOf("name" to "John Doe", "age" to "30", "city" to "New York"), rows[0])
    }

    @Test
    fun `test pipe-delimited CSV with headers`() {
        val csvContent = """
        name|age|city
        John Doe|30|New York
        Jane Smith|25|Los Angeles
    """.trimIndent()
        val reader = CSVReader(csvContent, '|')
        val rows = reader.generateRowMaps()
        assertEquals(mapOf("name" to "John Doe", "age" to "30", "city" to "New York"), rows[0])
    }

    @Test
    fun `test CSV with all fields quoted`() {
        val csvContent = """
        "name","age","city"
        "John Doe","30","New York"
        "Jane Smith","25","Los Angeles"
    """.trimIndent()
        val reader = CSVReader(csvContent, ',', quote = '"')
        val rows = reader.generateRowMaps()
        assertEquals(mapOf("name" to "John Doe", "age" to "30", "city" to "New York"), rows[0])
    }

    @Test
    fun `test CSV with mixed delimiters and quotes`() {
        val csvContent = """
        name,age,"city"
        John Doe,30,"New York, NY"
        "Jane Smith",25,Los Angeles
    """.trimIndent()
        val reader = CSVReader(csvContent, ',')
        val rows = reader.generateRowMaps()
        assertEquals(mapOf("name" to "John Doe", "age" to "30", "city" to "New York, NY"), rows[0])
    }

    @Test
    fun `test CSV with Windows line breaks`() {
        val csvContent = "name,age,city\r\n" +
                "John Doe,30,New York\r\n" +
                "Jane Smith,25,Los Angeles"
        val reader = CSVReader(csvContent, ',')
        val rows = reader.generateRowLists()
        assertEquals(listOf("John Doe", "30", "New York"), rows[1])
    }

    @Test
    fun `test CSV with 1 long line with a break`(){
        val csvContent = "2009-12-30\tKL1203\tSVG\tAMS\tMullers, Beer\tSelf\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t395.53\t\t\t0\t0\t0\t\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t\t\t\t\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t\t\t\t\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t\t\t\t\t\t\t\t\t\t\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t\t\t0\t\t\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t0\t\"times to be filled in from monthly overview!\n" +
                "Line Check\"\t\t\t0\t0\t0\t0\t0\t0\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tE190\tEMBRAER (Brazil)\tEMB-190/195\tJet\tAirplane\tMulti-Engine Land\t\t\t\t\t\t\t\t\t\t\t\t0\t0\n"
        val reader = CSVReader(csvContent, '\t')
        val rows = reader.generateRowLists()
        assertEquals(1, rows.size)
    }


}
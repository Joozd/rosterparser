package parsers

import java.io.File

abstract class ParserSubclassTest {
    abstract val testResourceName: String

    protected fun getResourceInputStream() = File(this::class.java.classLoader.getResource(testResourceName)!!.toURI())
        .inputStream()
}
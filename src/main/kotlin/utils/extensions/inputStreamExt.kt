package nl.joozd.rosterparser.utils.extensions

import java.io.InputStream

internal fun InputStream.makeReuseable() =
    readAllBytes().inputStream()
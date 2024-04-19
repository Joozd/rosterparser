package nl.joozd.rosterparser.utils.extensions

import java.io.InputStream

fun InputStream.makeReuseable() =
    readAllBytes().inputStream()
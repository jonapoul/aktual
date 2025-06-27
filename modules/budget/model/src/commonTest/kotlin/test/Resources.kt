package test

import java.io.File

internal fun getResource(name: String) =
  requireNotNull(System.getProperty("test.resourcesDir"))
    .let(::File)
    .resolve(name)
    .readText()

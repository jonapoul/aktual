package test

import java.io.File

internal fun getResource(name: String) = File(System.getProperty("test.resourcesDir"))
  .resolve(name)
  .readText()

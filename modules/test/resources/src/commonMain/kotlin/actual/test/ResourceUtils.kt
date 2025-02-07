package actual.test

import alakazam.test.core.getResourceAsStream

inline fun <reified T> T.getResourceAsText(filename: String): String = getResourceAsStream(filename)
  .reader()
  .readText()

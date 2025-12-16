package aktual.test

import assertk.Assert
import assertk.assertFailure
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.support.expected
import assertk.fail
import okio.FileSystem
import okio.Path
import java.io.File

fun <T> Assert<List<T>>.isEqualToList(vararg expected: T) = isEqualTo(expected.toList())

fun <K, V> Assert<Map<K, V>>.isEqualToMap(vararg expected: Pair<K, V>) = isEqualTo(expected.toMap())

inline fun <reified T : Throwable> assertFailsWith(f: () -> Unit) = assertFailure(f).isInstanceOf<T>()

fun Assert<Path>.existsOn(fileSystem: FileSystem) = given { path ->
  if (fileSystem.exists(path)) return@given
  expected("Expected $path to exist on $fileSystem")
}

fun Assert<Path>.doesNotExistOn(fileSystem: FileSystem) = given { path ->
  if (!fileSystem.exists(path)) return@given
  expected("Expected $path to not exist on $fileSystem")
}

fun <T : Throwable> Assert<T>.messageContains(expected: String) = transform { t ->
  val msg = t.message ?: fail("No message found in $t")
  if (msg.contains(expected)) t else expected("Expected message to contain '$expected', actually: '$msg'")
}

fun Assert<File>.contentEquals(expected: String) = given { file ->
  val contents = file.readText().removeSuffix("\n")
  if (contents == expected) return@given
  expected(
    "Unequal strings between expected{${expected.length}} and actual{${contents.length}}:\n" + diff(expected, contents),
  )
}

// Copied from assertk repo but they haven't published in ages
// https://github.com/assertk-org/assertk/blob/main/assertk/src/jvmMain/kotlin/assertk/assertions/file.kt
fun Assert<File>.doesNotExist() = transform { actual ->
  if (!actual.exists()) actual else expected("$actual not to exist")
}

fun Assert<String>.equalsDiffed(expected: String) = transform { actual ->
  val stripped = actual.removeSuffix("\n")
  if (expected == stripped) actual else expected(
    "Unequal strings between expected{${expected.length}} and actual{${stripped.length}}:\n" + diff(expected, stripped),
  )
}

fun diff(expected: String, actual: String): String {
  val expectedLines = expected.lines()
  val actualLines = actual.lines()

  // Build LCS (Longest Common Subsequence) table to align matching lines
  val dp = Array(expectedLines.size + 1) { IntArray(actualLines.size + 1) }
  for (i in expectedLines.indices.reversed()) {
    for (j in actualLines.indices.reversed()) {
      dp[i][j] = if (expectedLines[i] == actualLines[j]) {
        1 + dp[i + 1][j + 1]
      } else {
        maxOf(dp[i + 1][j], dp[i][j + 1])
      }
    }
  }

  return buildString {
    var i = 0
    var j = 0

    while (i < expectedLines.size || j < actualLines.size) {
      when {
        // Case 1: Lines are equal -> output as unchanged
        i < expectedLines.size && j < actualLines.size && expectedLines[i] == actualLines[j] -> {
          appendLine("    ${expectedLines[i]}")
          i++
          j++
        }

        // Case 2: Prefer consuming a line from actual if it leads to a longer match later. This means a line was
        // inserted in actual that doesn't exist in expected.
        j < actualLines.size && (i == expectedLines.size || dp[i][j + 1] >= dp[i + 1][j]) -> {
          appendLine("--- ${actualLines[j]}")
          j++
        }

        // Case 3: consume a line from expected. This means a line was removed from actual compared to expected
        i < expectedLines.size -> {
          appendLine("+++ ${expectedLines[i]}")
          i++
        }
      }
    }
  }
}

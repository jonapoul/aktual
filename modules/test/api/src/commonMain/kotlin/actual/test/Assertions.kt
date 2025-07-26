package actual.test

import actual.api.client.ActualJson
import org.intellij.lang.annotations.Language
import kotlin.test.assertEquals

inline fun <reified Model> testDecoding(@Language("JSON") json: String, expected: Model) {
  val actual = ActualJson.decodeFromString<Model>(json)
  assertEquals(expected = expected, actual = actual)
}

inline fun <reified Model> testEncoding(@Language("JSON") expected: String, model: Model) {
  val actual = PrettyJson.encodeToString<Model>(model)
  assertEquals(expected = expected, actual = actual)
}

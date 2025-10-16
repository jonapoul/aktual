package actual.test

import actual.api.client.ActualJson
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.intellij.lang.annotations.Language

inline fun <reified Model> testDecoding(
  @Language("JSON") json: String,
  expected: Model,
) = assertThat(ActualJson.decodeFromString<Model>(json)).isEqualTo(expected)

inline fun <reified Model> testEncoding(
  @Language("JSON") expected: String,
  model: Model,
) = assertThat(ActualJson.encodeToString<Model>(model)).isEqualTo(expected)

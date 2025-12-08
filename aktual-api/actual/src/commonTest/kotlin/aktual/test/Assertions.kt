package aktual.test

import aktual.api.client.AktualJson
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.intellij.lang.annotations.Language

inline fun <reified Model> testDecoding(
  @Language("JSON") json: String,
  expected: Model,
) = assertThat(AktualJson.decodeFromString<Model>(json)).isEqualTo(expected)

inline fun <reified Model> testEncoding(
  @Language("JSON") expected: String,
  model: Model,
) = assertThat(PrettyJson.encodeToString<Model>(model)).isEqualTo(expected)

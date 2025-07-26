package actual.test

import actual.api.client.ActualJson
import kotlin.test.assertEquals

inline fun <reified Model> testDecoding(json: String, model: Model) {
  val decoded = ActualJson.decodeFromString<Model>(json)
  assertEquals(expected = model, actual = decoded)
}

package actual.test

import actual.api.client.ActualJson
import kotlin.test.assertEquals

inline fun <reified Model> Model.assertMatches(json: String) {
  val decoded = ActualJson.decodeFromString<Model>(json)
  assertEquals(expected = this, actual = decoded)
}

package actual.budget.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportLegendColorTest {
  @Serializable
  private data class TestData(val color: ReportLegendColor)

  @Test
  fun `Serialize hex colour`() {
    val initial = TestData(ReportLegendColor.Hex("#123456"))
    val serialized = Json.encodeToString(initial)
    assertEquals(actual = serialized, expected = """{"color":"#123456"}""")
    val deserialized = Json.decodeFromString<TestData>(serialized)
    assertEquals(expected = initial, actual = deserialized)
  }

  @Test
  fun `Serialize named colour`() {
    val initial = TestData(ReportLegendColor.Red)
    val serialized = Json.encodeToString(initial)
    assertEquals(actual = serialized, expected = """{"color":"var(--color-reportsRed)"}""")
    val deserialized = Json.decodeFromString<TestData>(serialized)
    assertEquals(expected = initial, actual = deserialized)
  }
}

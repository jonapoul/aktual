package actual.budget.model

import actual.test.PrettyJson
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DbMetadataTest {
  @Test
  fun parseData() {
    val json = """
      {
        "id": "My-Finances-e742ff8",
        "budgetName": "Test Budget",
        "userId": "583b50fe-3c55-42ca-9f09-a14ecd38677f",
        "resetClock": true,
        "cloudFileId": "cf2b43ee-8067-48ed-ab5b-4e4e5531056e",
        "groupId": "ee90358a-f73e-4aa5-a922-653190fd31b7",
        "lastUploaded": "2025-02-27",
        "encryptKeyId": null,
        "lastSyncedTimestamp": "2025-02-27T19:18:25.454Z-0000-93836f5283a57c87",
        "lastScheduleRun": "2025-03-03"
      }
    """.trimIndent()

    val model = DbMetadata(
      id = "My-Finances-e742ff8",
      budgetName = "Test Budget",
      cloudFileId = BudgetId("cf2b43ee-8067-48ed-ab5b-4e4e5531056e"),
      groupId = "ee90358a-f73e-4aa5-a922-653190fd31b7",
      encryptKeyId = null,
      resetClock = true,
      userId = "583b50fe-3c55-42ca-9f09-a14ecd38677f",
      lastUploaded = LocalDate.parse("2025-02-27"),
      lastScheduleRun = LocalDate.parse("2025-03-03"),
      lastSyncedTimestamp = Timestamp(
        instant = Instant.parse("2025-02-27T19:18:25.454Z"),
        counter = 0L,
        node = "93836f5283a57c87",
      ),
    )

    assertEquals(
      actual = PrettyJson.decodeFromString<DbMetadata>(json),
      expected = model,
    )
  }
}

package aktual.budget.model

import aktual.budget.model.DbMetadataKey.Companion.BudgetMonthCount
import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.model.DbMetadataKey.Companion.Id
import aktual.budget.model.DbMetadataKey.Companion.TransactionDensity
import aktual.test.PrettyJson
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

class DbMetadataTest {
  @Test
  fun `Read and write metadata`() {
    val json =
      """
      {
        "budgetName": "Test Budget",
        "budget.collapsed": [
          "2E1F5BDB-209B-43F9-AF2C-3CE28E380C00"
        ],
        "cloudFileId": "cf2b43ee-8067-48ed-ab5b-4e4e5531056e",
        "groupId": "ee90358a-f73e-4aa5-a922-653190fd31b7",
        "id": "My-Finances-e742ff8",
        "lastScheduleRun": "2025-03-03",
        "lastSyncedTimestamp": "2025-02-27T19:18:25.454Z-0000-93836f5283a57c87",
        "lastUploaded": "2025-02-27",
        "resetClock": true,
        "userId": "583b50fe-3c55-42ca-9f09-a14ecd38677f",
        "encryptKeyId": null
      }
      """
        .trimIndent()

    val expectedModel =
      DbMetadata(
        id = "My-Finances-e742ff8",
        budgetName = "Test Budget",
        budgetCollapsed = listOf("2E1F5BDB-209B-43F9-AF2C-3CE28E380C00"),
        cloudFileId = BudgetId("cf2b43ee-8067-48ed-ab5b-4e4e5531056e"),
        groupId = "ee90358a-f73e-4aa5-a922-653190fd31b7",
        encryptKeyId = null,
        resetClock = true,
        userId = "583b50fe-3c55-42ca-9f09-a14ecd38677f",
        lastUploaded = LocalDate.parse("2025-02-27"),
        lastScheduleRun = LocalDate.parse("2025-03-03"),
        lastSyncedTimestamp =
          Timestamp(
            instant = Instant.parse("2025-02-27T19:18:25.454Z"),
            counter = 0L,
            node = "93836f5283a57c87",
          ),
      )

    val decoded = PrettyJson.decodeFromString<DbMetadata>(json)
    assertThat(decoded).isEqualTo(expectedModel)

    val encoded = PrettyJson.encodeToString(decoded)
    assertThat(encoded).isEqualTo(encoded)
  }

  @Test
  fun `Read and write a number`() {
    val metadata = DbMetadata(BudgetMonthCount to 3)
    val json = PrettyJson.encodeToString(metadata)
    assertThat(PrettyJson.decodeFromString<DbMetadata>(json)).isEqualTo(metadata)
  }

  @Test
  fun `Read and write an enum`() {
    val metadata = DbMetadata(TransactionDensity to TransactionsDensity.Comfortable)
    val json = PrettyJson.encodeToString(metadata)
    assertThat(PrettyJson.decodeFromString<DbMetadata>(json)).isEqualTo(metadata)
  }

  @Test
  fun `Key defined elsewhere decodes as its own type`() {
    val json = """{ "feature.density": "Dense", "feature.flag": true }"""
    val decoded = PrettyJson.decodeFromString<DbMetadata>(json)
    assertThat(decoded[DbMetadataKey.serializableKey<TransactionsDensity>("feature.density")])
      .isEqualTo(Dense)
    assertThat(decoded[DbMetadataKey.boolKey("feature.flag")]).isEqualTo(true)
  }

  @Test
  fun `Keys with the same name are equal`() {
    assertThat(DbMetadataKey.serializableKey<TransactionsDensity>("x"))
      .isEqualTo(DbMetadataKey.serializableKey<TransactionsDensity>("x"))
    val metadata =
      DbMetadata(
        DbMetadataKey.serializableKey<TransactionsDensity>("x") to TransactionsDensity.Dense,
      )
    assertThat(metadata[DbMetadataKey.serializableKey<TransactionsDensity>("x")]).isEqualTo(Dense)
  }

  @Test
  fun `Unknown arrays and objects round-trip`() {
    val json =
      """
      {
        "budgetName": "Test Budget",
        "unknown.array": [1, "two", { "three": 3 }],
        "unknown.object": { "a": [true] }
      }
      """
        .trimIndent()
    val decoded = PrettyJson.decodeFromString<DbMetadata>(json)
    assertThat(decoded[BudgetName]).isEqualTo("Test Budget")
    val reencoded = PrettyJson.decodeFromString<DbMetadata>(PrettyJson.encodeToString(decoded))
    assertThat(reencoded).isEqualTo(decoded)
    assertThat(reencoded.data.keys).containsOnly("budgetName", "unknown.array", "unknown.object")
  }

  @Test
  fun `Undecodable values read as null without failing others`() {
    val json = """{ "budget.monthCount": "x", "id": "abc" }"""
    val decoded = PrettyJson.decodeFromString<DbMetadata>(json)
    assertThat(decoded[BudgetMonthCount]).isNull()
    assertThat(decoded[Id]).isEqualTo("abc")
  }

  @Test
  fun `Unknown enum value reads as the fallback`() {
    val json = """{ "transactionDensity": "Squashed" }"""
    val decoded = PrettyJson.decodeFromString<DbMetadata>(json)
    assertThat(decoded[TransactionDensity]).isEqualTo(Compact)
  }

  @Test
  fun `Enum written by serial name`() {
    val metadata = DbMetadata(TransactionDensity to TransactionsDensity.Comfortable)
    assertThat(PrettyJson.encodeToString(metadata)).contains("\"Comfortable\"")
  }
}

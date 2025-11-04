/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

import aktual.test.PrettyJson
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.time.Instant

class DbMetadataTest {
  @Test
  fun `Read and write metadata`() {
    val json = """
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
    """.trimIndent()

    val expectedModel = DbMetadata(
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
      lastSyncedTimestamp = Timestamp(
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
}

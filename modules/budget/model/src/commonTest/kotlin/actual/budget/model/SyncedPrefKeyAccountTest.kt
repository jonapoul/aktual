package actual.budget.model

import actual.budget.model.SyncedPrefKey.PerAccount
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncedPrefKeyAccountTest {
  @Test
  fun decoding() {
    assertEquals(
      expected = PerAccount.CsvDelimiter(AccountId("abc-123")),
      actual = SyncedPrefKey.decode("csv-delimiter-abc-123"),
    )
  }
}

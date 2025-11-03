/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

import aktual.budget.model.SyncedPrefKey.PerAccount
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SyncedPrefKeyAccountTest {
  @Test
  fun decoding() {
    assertThat(SyncedPrefKey.decode("csv-delimiter-abc-123"))
      .isEqualTo(PerAccount.CsvDelimiter(AccountId("abc-123")))
  }
}

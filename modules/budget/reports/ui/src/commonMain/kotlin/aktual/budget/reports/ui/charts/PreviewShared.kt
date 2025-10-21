/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

internal object PreviewShared {
  const val WIDTH = 800
  const val HEIGHT = 200

  val AMOUNT = Amount(12345.67)

  val START_DATE = LocalDate(2024, Month.JANUARY, 1)

  val END_DATE = LocalDate(2025, Month.JULY, 6)
}

/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.DateRangeMode
import aktual.budget.reports.vm.SpendingComparison
import aktual.budget.reports.vm.SpendingData
import aktual.budget.reports.vm.SpendingDay
import aktual.budget.reports.vm.SpendingDayNumber
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

internal object PreviewSpending {
  val JUL_2025 = SpendingData(
    title = "Monthly Spending",
    mode = DateRangeMode.Live,
    targetMonth = YearMonth(2025, Month.JULY),
    comparison = SpendingComparison.Average,
    difference = Amount(534.88),
    days = persistentListOf(
      day(1, 82.48, 13.74),
      day(2, -56.55, 29.16),
      day(3, 0.52, 44.08),
      day(4, 8.62, 50.87),
      day(5, 55.09, 73.65),
      day(6, 60.69, 82.01),
      day(7, 149.0, 107.97),
      day(8, 149.0, 144.40),
      day(9, 158.0, 172.7),
      day(10, 226.61, 188.62),
      day(11, 226.61, 264.62),
      day(12, 243.35, 303.2),
      day(13, 243.35, 309.65),
      day(14, 275.35, 313.26),
      day(15, 275.35, 332.52),
      day(16, 867.2, 335.5),
      day(17, 917.2, 393.63),
      day(18, 976.69, 399.63),
      day(19, null, 441.81),
      day(20, null, 441.81),
      day(21, null, 445.81),
      day(22, null, 445.81),
      day(23, null, 455.37),
      day(24, null, 570.39),
      day(25, null, 570.39),
      day(26, null, 590.48),
      day(27, null, 543.64),
      SpendingDay(SpendingDayNumber.End, target = null, Amount(876.26)),
    ),
  )

  private fun day(number: Int, target: Double?, comparison: Double) = SpendingDay(
    number = SpendingDayNumber.Specific(number),
    target = target?.let(::Amount),
    comparison = Amount(comparison),
  )
}

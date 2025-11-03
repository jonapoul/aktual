/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.reports.vm

import aktual.budget.model.WidgetId
import androidx.compose.runtime.Immutable

@Immutable
data class ReportDashboardItem(
  val id: WidgetId,
  val name: String,
  val data: ChartData,
)

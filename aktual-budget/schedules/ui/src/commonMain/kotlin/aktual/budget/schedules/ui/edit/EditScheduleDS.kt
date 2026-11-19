package aktual.budget.schedules.ui.edit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

internal object EditScheduleDS {
  val sectionSpacing = 20.dp
  val fieldSpacing = 12.dp
  val badgeSpacing = 10.dp
  val dayWidth = 36.dp
  val changedEdge = 3.dp

  // Lines up with SettingRow's content
  val rowPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

  // Matches SectionLabel's padding
  val sectionLabelPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = 8.dp)

  // AktualTextField's minimum height
  val shimmerNameHeight = 40.dp
  val shimmerBadgeWidth = 72.dp
  val shimmerBadgeHeight = 22.dp
  val shimmerLabelWidth = 80.dp
  val shimmerValueWidth = 110.dp
}

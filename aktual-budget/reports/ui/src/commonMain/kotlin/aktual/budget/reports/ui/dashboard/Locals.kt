package aktual.budget.reports.ui.dashboard

import aktual.budget.model.CustomReportId
import androidx.compose.runtime.compositionLocalOf
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

// False once the home page has no room for another pinned report
internal val LocalCanPinReports = compositionLocalOf { false }

// Names already taken by custom reports, to reject duplicates when renaming one
internal val LocalCustomReportNames =
  compositionLocalOf<ImmutableMap<CustomReportId, String>> { persistentMapOf() }

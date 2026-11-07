package aktual.crashes.vm

import aktual.core.logging.CrashReport
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface CrashReportsState {
  data object Loading : CrashReportsState

  data object Empty : CrashReportsState

  data class Loaded(val reports: ImmutableList<CrashReport>) : CrashReportsState
}

@Immutable
sealed interface CrashReportState {
  data object Loading : CrashReportState

  data object Missing : CrashReportState

  data class Loaded(val report: CrashReport, val issueUrl: String) : CrashReportState
}

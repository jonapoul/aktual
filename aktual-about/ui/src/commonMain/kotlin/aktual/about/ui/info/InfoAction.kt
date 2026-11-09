package aktual.about.ui.info

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface InfoAction

internal data object NavBack : InfoAction

internal data object ViewLicenses : InfoAction

internal data object CheckUpdates : InfoAction

internal data object ReportIssue : InfoAction

internal data object ManageStorage : InfoAction

internal data object AppLogs : InfoAction

internal data object ViewCrashReports : InfoAction

internal data class OpenUrl(val url: String) : InfoAction

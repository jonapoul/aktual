package aktual.prefs.ui.schedules

import androidx.compose.runtime.Immutable

internal sealed interface ScheduleSettingsAction

internal data object NavBack : ScheduleSettingsAction

@Immutable
internal fun interface ScheduleSettingsActionHandler {
  operator fun invoke(action: ScheduleSettingsAction)
}

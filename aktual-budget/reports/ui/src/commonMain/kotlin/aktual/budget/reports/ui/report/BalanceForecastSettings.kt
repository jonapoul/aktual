package aktual.budget.reports.ui.report

import aktual.budget.model.AccountId
import aktual.budget.reports.vm.BalanceForecastReportMeta
import aktual.budget.reports.vm.ForecastGranularity
import aktual.budget.reports.vm.ForecastSource
import aktual.budget.reports.vm.QueryEditorData
import aktual.budget.reports.vm.forecastTimeFrame
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet

// packages/desktop-client/src/components/reports/reports/BalanceForecast.tsx
@Composable
internal fun BalanceForecastSettings(
  meta: BalanceForecastReportMeta,
  editorData: QueryEditorData?,
  onEdit: ((BalanceForecastReportMeta) -> BalanceForecastReportMeta) -> Unit,
  onDismiss: () -> Unit,
) {
  var pickAccounts by rememberSaveable { mutableStateOf(false) }

  if (pickAccounts && editorData != null) {
    AccountSheet(
      title = Strings.reportsBalanceForecastAccounts,
      accounts = editorData.allAccounts,
      selected = meta.selectedAccounts(editorData),
      // Picking every account goes back to unset, which also counts schedules with no account
      onChange = { selected ->
        val all = selected.size == editorData.allAccounts.size
        onEdit { it.copy(accounts = if (all) null else selected.toList()) }
      },
      onDismiss = { pickAccounts = false },
    )
    return
  }

  // The tracking budget forecast doesn't use schedules, so accounts and daily points don't
  // apply to it
  val isTracking = editorData?.isTrackingBudget == true && meta.source == TrackingBudget

  FilteredReportSettings(
    timeFrame = meta.forecastTimeFrame(),
    conditions = meta.conditions.orEmpty().toImmutableList(),
    conditionsOp = meta.conditionsOp ?: And,
    editorData = editorData,
    onTimeFrame = { timeFrame -> onEdit { it.copy(timeFrame = timeFrame) } },
    onConditions = { conditions, op ->
      onEdit { it.copy(conditions = conditions, conditionsOp = op) }
    },
    onDismiss = onDismiss,
  ) {
    if (editorData?.isTrackingBudget == true) {
      Column {
        SectionLabel(Strings.reportsBalanceForecastSource)
        AktualSlidingToggleButton(
          modifier = Modifier.fillMaxWidth(),
          selected = if (isTracking) TrackingBudget else Schedules,
          options = SOURCES,
          onSelect = { source -> onEdit { it.copy(source = source) } },
          string = { it.label() },
        )
      }
    }

    if (!isTracking) {
      Column {
        SectionLabel(Strings.reportsBalanceForecastGranularity)
        AktualSlidingToggleButton(
          modifier = Modifier.fillMaxWidth(),
          selected = meta.granularity?.takeIf { it != Unknown } ?: Monthly,
          options = GRANULARITIES,
          onSelect = { granularity -> onEdit { it.copy(granularity = granularity) } },
          string = { it.label() },
        )
      }
    }

    if (editorData != null && !isTracking) {
      Section(label = Strings.reportsCustomEditorDisplay) {
        SettingValueRow(
          Strings.reportsBalanceForecastAccounts,
          countLabel(meta.selectedAccounts(editorData).size, editorData.allAccounts.size),
        ) {
          pickAccounts = true
        }
      }
    }
  }
}

private fun BalanceForecastReportMeta.selectedAccounts(
  data: QueryEditorData,
): ImmutableSet<AccountId> =
  (accounts?.toSet() ?: data.allAccounts.map { AccountId(it.id) }.toSet()).toImmutableSet()

@Composable
private fun ForecastSource.label(): String =
  when (this) {
    TrackingBudget -> Strings.reportsBalanceForecastSourceTracking
    Schedules,
    Unknown -> Strings.reportsBalanceForecastSourceSchedules
  }

@Composable
private fun ForecastGranularity.label(): String =
  when (this) {
    Daily -> Strings.reportsBalanceForecastDaily
    Monthly,
    Unknown -> Strings.reportsBalanceForecastMonthly
  }

private val SOURCES = persistentListOf<ForecastSource>(Schedules, TrackingBudget)

private val GRANULARITIES = persistentListOf<ForecastGranularity>(Monthly, Daily)

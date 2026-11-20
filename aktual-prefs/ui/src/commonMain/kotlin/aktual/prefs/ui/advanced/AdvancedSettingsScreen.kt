package aktual.prefs.ui.advanced

import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formatted
import aktual.core.ui.plainTextClipEntry
import aktual.prefs.components.BasicPreferenceItem
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.advanced.AdvancedSettingsState
import aktual.prefs.vm.advanced.AdvancedSettingsViewModel
import aktual.prefs.vm.advanced.RepairState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
fun AdvancedSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: AdvancedSettingsViewModel = metroViewModel<AdvancedSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val clipboard = LocalClipboard.current
  val scope = rememberCoroutineScope()
  AdvancedSettingsScaffold(
    state = state,
    highlight = highlight,
    onBack = back::invoke,
    onCopy = { text -> scope.launch { clipboard.setClipEntry(plainTextClipEntry(text)) } },
    onRepair = viewModel::repairTransactions,
  )
}

@Composable
private fun AdvancedSettingsScaffold(
  state: AdvancedSettingsState,
  onBack: () -> Unit,
  onCopy: (String) -> Unit,
  onRepair: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  SettingsListScaffold(
    title = Strings.settingsAdvanced,
    description = null,
    onBack = onBack,
    highlight = highlight,
  ) {
    item {
      PreferenceGroup(
        title = Strings.settingsAdvancedBudgetFile,
        subtitle = Strings.settingsAdvancedBudgetFileDesc,
      ) {
        IdItem(
          title = Strings.settingsAdvancedBudgetId,
          subtitle = Strings.settingsAdvancedBudgetIdDesc,
          id = state.budgetId,
          onCopy = onCopy,
        )
        if (state.syncId != null) {
          IdItem(
            title = Strings.settingsAdvancedSyncId,
            subtitle = Strings.settingsAdvancedSyncIdDesc,
            id = state.syncId,
            onCopy = onCopy,
          )
        }
      }
    }

    item {
      PreferenceGroup(title = Strings.settingsAdvancedTroubleshooting) {
        // Upstream only offers it for a budget on a server
        if (state.syncId != null) ResetSyncItem()
        RepairItem(state.repair, onRepair)
      }
    }
  }
}

@Composable
private fun IdItem(title: String, subtitle: String, id: String?, onCopy: (String) -> Unit) {
  BasicPreferenceItem(
    title = title,
    subtitle = subtitle,
    icon = null,
    modifier = Modifier.padding(vertical = 2.dp),
    onClick = id?.let { { onCopy(it) } },
    backgroundColor = id?.let { colors.tableRowBackgroundHover },
    bottomContent = {
      if (id != null) {
        Text(
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
          text = id,
          style = typography.bodyMedium,
          color = colors.settingsCardText,
        )
      }
    },
  )
}

// Needs the budget file upload that Aktual doesn't have yet
@Composable
private fun ResetSyncItem() {
  BasicPreferenceItem(
    title = Strings.settingsAdvancedResetSync,
    subtitle = Strings.settingsAdvancedResetSyncDesc,
    icon = null,
    onClick = null,
    enabled = false,
    bottomContent = {
      Row(
        modifier = Modifier.padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = CenterVertically,
      ) {
        AktualButton(
          text = Strings.settingsAdvancedResetSyncButton,
          kind = Destructive,
          variant = Secondary,
          onClick = {},
          isEnabled = false,
        )
        Text(
          text = Strings.settingsAdvancedUnsupported,
          style = typography.labelLarge,
          color = colors.settingsCardTextSubdued,
        )
      }
    },
  )
}

@Composable
private fun RepairItem(state: RepairState, onRepair: () -> Unit) {
  BasicPreferenceItem(
    title = Strings.settingsAdvancedRepair,
    subtitle = Strings.settingsAdvancedRepairDesc,
    icon = null,
    onClick = null,
    bottomContent = {
      Column(
        modifier = Modifier.padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        RepairDetails()
        AktualButton(
          text = Strings.settingsAdvancedRepairButton,
          kind = Destructive,
          variant = Secondary,
          onClick = onRepair,
          isLoading = state == Running,
        )
        RepairResults(state)
      }
    },
  )
}

@Composable
private fun RepairDetails() {
  var isExpanded by remember { mutableStateOf(false) }
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(
      modifier = Modifier.clickable { isExpanded = !isExpanded }.padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = CenterVertically,
    ) {
      Icon(
        modifier = Modifier.size(16.dp).rotate(if (isExpanded) 0f else COLLAPSED_ROTATION),
        imageVector = AktualIcons.CheveronDown,
        contentDescription = null,
        tint = colors.settingsCardText,
      )
      Text(
        text = Strings.settingsAdvancedRepairDetails,
        style = typography.labelLarge,
        color = colors.settingsCardText,
      )
    }

    AnimatedVisibility(
      visible = isExpanded,
      enter = fadeIn() + expandVertically(expandFrom = Top),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Top),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Bullet(Strings.settingsAdvancedRepairDetailDeleted)
        Bullet(Strings.settingsAdvancedRepairDetailParent)
        Bullet(Strings.settingsAdvancedRepairDetailAmounts)
        Bullet(Strings.settingsAdvancedRepairDetailErrors)
        Bullet(Strings.settingsAdvancedRepairDetailTransfers)
        Bullet(Strings.settingsAdvancedRepairDetailParentCategory)
      }
    }
  }
}

@Composable
private fun Bullet(text: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(text = "•", style = typography.labelLarge, color = colors.settingsCardTextSubdued)
    Text(text = text, style = typography.labelLarge, color = colors.settingsCardTextSubdued)
  }
}

@Composable
private fun RepairResults(state: RepairState) {
  when (state) {
    Idle,
    Running -> Unit
    Failed ->
      Text(
        text = Strings.settingsAdvancedRepairFailed,
        style = typography.labelLarge,
        color = colors.errorText,
      )
    is Done ->
      Text(
        text = state.summary(),
        style = typography.labelLarge,
        color = if (state.mismatchedSplits.isEmpty()) colors.noticeTextLight else colors.errorText,
      )
  }
}

@Composable
private fun RepairState.Done.summary(): String {
  if (isEmpty) return Strings.settingsAdvancedRepairNone
  val lines = buildList {
    if (blankPayees > 0) add(Plurals.settingsAdvancedRepairBlankPayees(blankPayees, blankPayees))
    if (cleared > 0) add(Plurals.settingsAdvancedRepairCleared(cleared, cleared))
    if (deleted > 0) add(Plurals.settingsAdvancedRepairDeleted(deleted, deleted))
    if (nonParentErrors > 0) {
      add(Plurals.settingsAdvancedRepairErrors(nonParentErrors, nonParentErrors))
    }
    if (transfers > 0) add(Plurals.settingsAdvancedRepairTransfers(transfers, transfers))
    if (mismatchedSplits.isNotEmpty()) {
      val dates = mismatchedSplits.map { "- ${it.formatted()}" }
      add(Plurals.settingsAdvancedRepairMismatched(mismatchedSplits.size, mismatchedSplits.size))
      addAll(dates)
    }
    if (categorisedParents > 0) {
      add(Plurals.settingsAdvancedRepairParentCategories(categorisedParents, categorisedParents))
    }
  }
  return lines.joinToString(separator = "\n")
}

// The details chevron points right while they are hidden
private const val COLLAPSED_ROTATION = -90f

@PortraitPreview
@Composable
private fun PreviewAdvancedSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    AdvancedSettingsScaffold(
      state =
        AdvancedSettingsState(
          budgetId = "My-Finances-1a2b3c4",
          syncId = "5f8e1c2a-4b3d-4e6f-9a7b-1c2d3e4f5a6b",
          repair =
            RepairState.Done(
              blankPayees = 2,
              cleared = 0,
              deleted = 1,
              transfers = 0,
              nonParentErrors = 0,
              categorisedParents = 0,
              mismatchedSplits = persistentListOf(LocalDate(2026, 10, 1)),
            ),
        ),
      onBack = {},
      onCopy = {},
      onRepair = {},
    )
  }

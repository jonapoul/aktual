package aktual.budget.banksync.ui

import aktual.budget.banksync.domain.MappedField
import aktual.budget.banksync.domain.TransactionDirection
import aktual.budget.banksync.vm.settings.BankSyncSettingsState
import aktual.budget.banksync.vm.settings.BankSyncSettingsViewModel
import aktual.budget.banksync.vm.settings.FieldOption
import aktual.budget.banksync.vm.settings.MappedFieldRow
import aktual.budget.model.AccountId
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.Badge
import aktual.core.icons.material.BarChart
import aktual.core.icons.material.CalendarToday
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Save
import aktual.core.icons.material.Timer
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LoadingScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import aktual.prefs.ui.core.BasicPreferenceItem
import aktual.prefs.ui.core.BooleanPreferenceItem
import aktual.prefs.ui.core.PreferenceGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun BankSyncSettingsScreen(
  id: AccountId,
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: BankSyncSettingsViewModel = bankSyncSettingsViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbar = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        Saved -> back()
        is SaveFailed -> snackbar.showSettingsSaveFailed(event.cause)
        Unlinked -> back()
        is UnlinkFailed -> snackbar.showUnlinkFailed(event.cause)
      }
    }
  }

  BankSyncSettingsScaffold(
    modifier = modifier,
    state = state,
    snackbarHostState = snackbar,
    onAction = { action ->
      when (action) {
        NavigateBack -> back()
        SaveSettings -> viewModel.save()
        UnlinkAccount -> viewModel.unlink()
        is SetToggle -> viewModel.set(action.toggle, action.value)
        is SetDirection -> viewModel.setDirection(action.direction)
        is SetMapping -> viewModel.setMapping(action.field, action.value)
      }
    },
  )
}

@Composable
private fun bankSyncSettingsViewModel(id: AccountId) =
  assistedMetroViewModel<BankSyncSettingsViewModel, BankSyncSettingsViewModel.Factory>(
    key = id.value
  ) {
    create(id)
  }

@Composable
private fun BankSyncSettingsScaffold(
  state: BankSyncSettingsState,
  onAction: BankSyncSettingsActionHandler,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(NavigateBack) }) },
        title = {
          Column {
            Text(text = Strings.bankSyncSettingsTitle)
            if (state is Editing) {
              Text(
                text = state.accountName ?: Strings.bankSyncUnnamedAccount,
                style = typography.bodySmall,
                color = colors.pageTextSubdued,
                maxLines = 1,
                overflow = Ellipsis,
              )
            }
          }
        },
        actions = {
          if (state is Editing) {
            BareIconButton(
              imageVector = MaterialIcons.Save,
              contentDescription = Strings.bankSyncSettingsSave,
              enabled = state.hasChanges,
              onClick = { onAction(SaveSettings) },
            )
          }
        },
      )
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        Loading -> {
          LoadingScreen(modifier = Modifier.padding(innerPadding))
        }
        is Failure -> {
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.bankSyncSettingsFailureTitle,
            reason = state.cause ?: Strings.bankSyncSettingsFailureMissing,
            action =
              FailureAction(
                text = { Strings.navBack },
                icon = MaterialIcons.ArrowBack,
                onClick = { onAction(NavigateBack) },
              ),
          )
        }
        is Editing -> {
          BankSyncSettingsContent(
            modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
            state = state,
            scrollState = scrollState,
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
            onAction = onAction,
          )
        }
      }
    }
  }
}

@Composable
private fun BankSyncSettingsContent(
  state: BankSyncSettingsState.Editing,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  onAction: BankSyncSettingsActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScrollWithBar(scrollState)
        .padding(contentPadding)
        .padding(BankSyncDS.settingsPadding),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsItemSpacing),
  ) {
    // Investment accounts only import their balance, so have no transactions to map
    if (state.importTransactions) {
      FieldMappingGroup(
        direction = state.direction,
        fields = state.fields,
        onAction = onAction,
      )
    }

    OptionsGroup(state = state, onAction = onAction)

    UnlinkButton(accountName = state.accountName, onConfirm = { onAction(UnlinkAccount) })

    BottomSpacing()
  }
}

@Composable
private fun UnlinkButton(
  accountName: String?,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showConfirm by remember { mutableStateOf(false) }

  NormalTextButton(
    modifier = modifier.fillMaxWidth(),
    text = Strings.bankSyncSettingsUnlink,
    onClick = { showConfirm = true },
  )

  if (showConfirm) {
    AktualAlertDialog(
      title = Strings.bankSyncSettingsUnlinkTitle,
      onDismissRequest = { showConfirm = false },
      buttons = {
        TextButton(onClick = { showConfirm = false }) {
          Text(Strings.bankSyncSettingsUnlinkCancel)
        }
        TextButton(
          onClick = {
            showConfirm = false
            onConfirm()
          }
        ) {
          Text(Strings.bankSyncSettingsUnlinkConfirm, color = colors.errorText)
        }
      },
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(BankSyncDS.headerSpacing)) {
        Text(Strings.bankSyncSettingsUnlinkMessage(accountName ?: Strings.bankSyncUnnamedAccount))
        Text(Strings.bankSyncSettingsUnlinkDetail)
      }
    }
  }
}

@Composable
private fun FieldMappingGroup(
  direction: TransactionDirection,
  fields: ImmutableList<MappedFieldRow>?,
  onAction: BankSyncSettingsActionHandler,
  modifier: Modifier = Modifier,
) {
  PreferenceGroup(
    modifier = modifier.fillMaxWidth(),
    title = Strings.bankSyncSettingsMapping,
    subtitle = if (fields == null) Strings.bankSyncSettingsNoFields else null,
  ) {
    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth().padding(BankSyncDS.settingsTogglePadding),
      selected = direction,
      options = Directions,
      onSelect = { onAction(SetDirection(it)) },
      string = { directionString(it) },
    )

    fields?.fastForEach { row ->
      MappedFieldItem(row = row, onSelect = { onAction(SetMapping(row.field, it)) })
    }
  }
}

@Composable
private fun MappedFieldItem(
  row: MappedFieldRow,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val options = remember(row.options) { row.options.map { it.field }.toImmutableList() }

  BasicPreferenceItem(
    modifier = modifier,
    title = fieldString(row.field),
    subtitle = row.example?.let { Strings.bankSyncSettingsExample(it) },
    icon = fieldIcon(row.field),
    onClick = null,
    includeBackground = false,
    bottomContent = {
      AktualExposedDropDownMenu(
        modifier = Modifier.fillMaxWidth(),
        value = row.selected,
        onValueChange = { it?.let(onSelect) },
        options = options,
        string = { it ?: Strings.bankSyncSettingsUnmapped },
        isEnabled = options.isNotEmpty(),
      )
    },
  )
}

@Composable
private fun OptionsGroup(
  state: BankSyncSettingsState.Editing,
  onAction: BankSyncSettingsActionHandler,
  modifier: Modifier = Modifier,
) {
  PreferenceGroup(
    modifier = modifier.fillMaxWidth(),
    title = Strings.bankSyncSettingsOptions,
    subtitle = null,
  ) {
    BooleanPreferenceItem(
      value = state.importPending,
      onValueChange = { onAction(SetToggle(ImportPending, it)) },
      title = Strings.bankSyncSettingsImportPending,
      subtitle = null,
      icon = MaterialIcons.Timer,
      enabled = state.importTransactions,
      includeBackground = false,
    )
    BooleanPreferenceItem(
      value = state.importNotes,
      onValueChange = { onAction(SetToggle(ImportNotes, it)) },
      title = Strings.bankSyncSettingsImportNotes,
      subtitle = null,
      icon = MaterialIcons.Edit,
      enabled = state.importTransactions,
      includeBackground = false,
    )
    BooleanPreferenceItem(
      value = state.reimportDeleted,
      onValueChange = { onAction(SetToggle(ReimportDeleted, it)) },
      title = Strings.bankSyncSettingsReimportDeleted,
      subtitle = Strings.bankSyncSettingsReimportDeletedHint,
      icon = MaterialIcons.Refresh,
      enabled = state.importTransactions,
      includeBackground = false,
    )
    BooleanPreferenceItem(
      value = state.updateDates,
      onValueChange = { onAction(SetToggle(UpdateDates, it)) },
      title = Strings.bankSyncSettingsUpdateDates,
      subtitle = Strings.bankSyncSettingsUpdateDatesHint,
      icon = MaterialIcons.CalendarToday,
      enabled = state.importTransactions,
      includeBackground = false,
    )
    BooleanPreferenceItem(
      value = !state.importTransactions,
      onValueChange = { onAction(SetToggle(ImportTransactions, !it)) },
      title = Strings.bankSyncSettingsInvestment,
      subtitle = Strings.bankSyncSettingsInvestmentHint,
      icon = MaterialIcons.BarChart,
      includeBackground = false,
    )
  }
}

private val Directions = TransactionDirection.entries.toImmutableList()

@Composable
private fun directionString(direction: TransactionDirection): String =
  when (direction) {
    Payment -> Strings.bankSyncSettingsPayment
    Deposit -> Strings.bankSyncSettingsDeposit
  }

@Composable
private fun fieldString(field: MappedField): String =
  when (field) {
    Date -> Strings.bankSyncSettingsFieldDate
    Payee -> Strings.bankSyncSettingsFieldPayee
    Notes -> Strings.bankSyncSettingsFieldNotes
  }

private fun fieldIcon(field: MappedField): ImageVector =
  when (field) {
    Date -> MaterialIcons.CalendarToday
    Payee -> MaterialIcons.Badge
    Notes -> MaterialIcons.Edit
  }

@PortraitPreview
@Composable
private fun PreviewBankSyncSettingsScaffold(
  @PreviewParameter(BankSyncSettingsStateProvider::class)
  params: ColoredParams<BankSyncSettingsState>
) = PreviewWithColoredParams(params) { BankSyncSettingsScaffold(state = this, onAction = {}) }

private val PreviewEditing =
  BankSyncSettingsState.Editing(
    accountName = "Current account",
    importTransactions = true,
    importPending = true,
    importNotes = true,
    reimportDeleted = true,
    updateDates = false,
    direction = Payment,
    fields =
      persistentListOf(
        MappedFieldRow(
          field = Date,
          selected = "date",
          options =
            persistentListOf(
              FieldOption("date", "2026-09-30"),
              FieldOption("bookingDate", "2026-09-30"),
            ),
        ),
        MappedFieldRow(
          field = Payee,
          selected = "payeeName",
          options =
            persistentListOf(
              FieldOption("payeeName", "Tesco"),
              FieldOption("creditorName", "TESCO STORES 1234"),
            ),
        ),
        MappedFieldRow(
          field = Notes,
          selected = "notes",
          options = persistentListOf(),
        ),
      ),
    hasChanges = true,
  )

private class BankSyncSettingsStateProvider :
  ColoredParameterProvider<BankSyncSettingsState>(
    PreviewEditing,
    PreviewEditing.copy(fields = null, hasChanges = false),
    PreviewEditing.copy(importTransactions = false),
    Loading,
    BankSyncSettingsState.Failure(cause = null),
  )

package aktual.budget.banksync.ui

import aktual.budget.banksync.domain.MappedField
import aktual.budget.banksync.domain.TransactionDirection
import aktual.budget.banksync.vm.settings.BankSyncSettingsEvent
import aktual.budget.banksync.vm.settings.BankSyncSettingsState
import aktual.budget.banksync.vm.settings.BankSyncSettingsViewModel
import aktual.budget.banksync.vm.settings.BankSyncToggle
import aktual.budget.banksync.vm.settings.FieldOption
import aktual.budget.banksync.vm.settings.MappedFieldRow
import aktual.budget.model.AccountId
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Save
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
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
import aktual.core.ui.switch
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
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
        BankSyncSettingsEvent.Saved -> back()
        is BankSyncSettingsEvent.SaveFailed -> snackbar.showSettingsSaveFailed(event.cause)
        BankSyncSettingsEvent.Unlinked -> back()
        is BankSyncSettingsEvent.UnlinkFailed -> snackbar.showUnlinkFailed(event.cause)
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
            if (state is BankSyncSettingsState.Editing) {
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
          if (state is BankSyncSettingsState.Editing) {
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
        BankSyncSettingsState.Loading -> {
          LoadingScreen(modifier = Modifier.padding(innerPadding))
        }
        is BankSyncSettingsState.Failure -> {
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
        is BankSyncSettingsState.Editing -> {
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
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(BankSyncDS.settingsPadding),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsSectionSpacing),
  ) {
    // Investment accounts only import their balance, so have no transactions to map
    if (state.importTransactions) {
      FieldMappingSection(
        direction = state.direction,
        fields = state.fields,
        onAction = onAction,
      )
    }

    OptionsSection(state = state, onAction = onAction)

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
      Column(verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsLabelSpacing)) {
        Text(Strings.bankSyncSettingsUnlinkMessage(accountName ?: Strings.bankSyncUnnamedAccount))
        Text(Strings.bankSyncSettingsUnlinkDetail)
      }
    }
  }
}

@Composable
private fun FieldMappingSection(
  direction: TransactionDirection,
  fields: ImmutableList<MappedFieldRow>?,
  onAction: BankSyncSettingsActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsLabelSpacing),
  ) {
    Text(text = Strings.bankSyncSettingsMapping, style = typography.labelLarge)

    AktualSlidingToggleButton(
      modifier = Modifier.fillMaxWidth(),
      selected = direction,
      options = Directions,
      onSelect = { onAction(SetDirection(it)) },
      string = { directionString(it) },
    )

    if (fields == null) {
      Text(
        text = Strings.bankSyncSettingsNoFields,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    } else {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .background(colors.tableBackground, CardShape)
            .border(Hairline, colors.tableBorder, CardShape)
            .padding(BankSyncDS.itemCardPadding),
        verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsFieldSpacing),
      ) {
        for (row in fields) {
          MappedFieldItem(row = row, onSelect = { onAction(SetMapping(row.field, it)) })
        }
      }
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

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(BankSyncDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = fieldString(row.field),
      style = typography.bodyLarge,
      color = colors.tableText,
    )

    Column(
      modifier = Modifier.weight(2f),
      verticalArrangement = Arrangement.spacedBy(BankSyncDS.itemContentSpacing),
    ) {
      AktualExposedDropDownMenu(
        value = row.selected,
        onValueChange = { it?.let(onSelect) },
        options = options,
        string = { it ?: Strings.bankSyncSettingsUnmapped },
        isEnabled = options.isNotEmpty(),
      )

      row.example?.let { example ->
        Text(
          text = Strings.bankSyncSettingsExample(example),
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
          maxLines = 2,
          overflow = Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun OptionsSection(
  state: BankSyncSettingsState.Editing,
  onAction: BankSyncSettingsActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsFieldSpacing),
  ) {
    Text(text = Strings.bankSyncSettingsOptions, style = typography.labelLarge)

    ToggleRow(
      title = Strings.bankSyncSettingsImportPending,
      checked = state.importPending,
      enabled = state.importTransactions,
      onChange = { onAction(SetToggle(BankSyncToggle.ImportPending, it)) },
    )
    ToggleRow(
      title = Strings.bankSyncSettingsImportNotes,
      checked = state.importNotes,
      enabled = state.importTransactions,
      onChange = { onAction(SetToggle(BankSyncToggle.ImportNotes, it)) },
    )
    ToggleRow(
      title = Strings.bankSyncSettingsReimportDeleted,
      hint = Strings.bankSyncSettingsReimportDeletedHint,
      checked = state.reimportDeleted,
      enabled = state.importTransactions,
      onChange = { onAction(SetToggle(BankSyncToggle.ReimportDeleted, it)) },
    )
    ToggleRow(
      title = Strings.bankSyncSettingsUpdateDates,
      hint = Strings.bankSyncSettingsUpdateDatesHint,
      checked = state.updateDates,
      enabled = state.importTransactions,
      onChange = { onAction(SetToggle(BankSyncToggle.UpdateDates, it)) },
    )
    ToggleRow(
      title = Strings.bankSyncSettingsInvestment,
      hint = Strings.bankSyncSettingsInvestmentHint,
      checked = !state.importTransactions,
      onChange = { onAction(SetToggle(BankSyncToggle.ImportTransactions, !it)) },
    )
  }
}

@Composable
private fun ToggleRow(
  title: String,
  checked: Boolean,
  onChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  hint: String? = null,
  enabled: Boolean = true,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(BankSyncDS.settingsFieldSpacing),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = typography.bodyLarge,
        color = if (enabled) colors.pageText else colors.pageTextSubdued,
      )
      hint?.let {
        Text(text = it, style = typography.bodySmall, color = colors.pageTextSubdued)
      }
    }
    Switch(
      checked = checked,
      onCheckedChange = onChange,
      enabled = enabled,
      colors = colors.switch(),
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
    direction = TransactionDirection.Payment,
    fields =
      persistentListOf(
        MappedFieldRow(
          field = MappedField.Date,
          selected = "date",
          options =
            persistentListOf(
              FieldOption("date", "2026-09-30"),
              FieldOption("bookingDate", "2026-09-30"),
            ),
        ),
        MappedFieldRow(
          field = MappedField.Payee,
          selected = "payeeName",
          options =
            persistentListOf(
              FieldOption("payeeName", "Tesco"),
              FieldOption("creditorName", "TESCO STORES 1234"),
            ),
        ),
        MappedFieldRow(
          field = MappedField.Notes,
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
    BankSyncSettingsState.Loading,
    BankSyncSettingsState.Failure(cause = null),
  )

package aktual.budget.accounts.ui

import aktual.budget.accounts.vm.CreateAccountState
import aktual.budget.accounts.vm.CreateAccountViewModel
import aktual.budget.model.AccountId
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.AmountField
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ColorParams
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButtonWithLoading
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.switch
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CreateAccountScreen(
  back: BackNavigator,
  toAccount: (AccountId) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CreateAccountViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val currentToAccount by rememberUpdatedState(toAccount)

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        is Created -> currentToAccount(event.id)
      }
    }
  }

  CreateAccountScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        NavigateBack -> back()
        Create -> viewModel.create()
        DismissSaveError -> viewModel.dismissSaveError()
        is SetName -> viewModel.setName(action.name)
        is SetBalance -> viewModel.setBalance(action.balance)
        is SetOffBudget -> viewModel.setOffBudget(action.offBudget)
        is SetNegative -> viewModel.setNegative(action.negative)
      }
    },
  )
}

@Composable
private fun CreateAccountScaffold(
  state: CreateAccountState,
  onAction: CreateAccountActionHandler,
  modifier: Modifier = Modifier,
) {
  val nameState = rememberTextFieldState()
  val balanceState = rememberTextFieldState()

  LaunchedEffect(Unit) {
    snapshotFlow { nameState.text.toString() }.collect { onAction(SetName(it)) }
  }
  LaunchedEffect(Unit) {
    snapshotFlow { balanceState.text.toString() }.collect { onAction(SetBalance(it)) }
  }

  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(NavigateBack) }) },
        title = { Text(Strings.accountsCreateTitle) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      Column(
        modifier =
          Modifier.hazedTopBarContent(hazeState, innerPadding)
            .fillMaxSize()
            .verticalScrollWithBar(scrollState)
            .padding(hazedTopBarContentPadding(hazeState, innerPadding))
            .padding(CreateAccountDS.contentPadding),
        verticalArrangement = Arrangement.spacedBy(CreateAccountDS.fieldSpacing),
      ) {
        CreateAccountForm(
          state = state,
          nameState = nameState,
          balanceState = balanceState,
          onAction = onAction,
        )
        BottomSpacing()
      }

      state.saveError?.let { message ->
        AktualAlertDialog(
          title = Strings.accountsCreateFailureTitle,
          highlight = colors.errorText,
          onDismissRequest = { onAction(DismissSaveError) },
          buttons = {
            TextButton(onClick = { onAction(DismissSaveError) }) {
              Text(Strings.accountsCreateFailureDismiss)
            }
          },
          content = { Text(message) },
        )
      }
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.CreateAccountForm(
  state: CreateAccountState,
  nameState: TextFieldState,
  balanceState: TextFieldState,
  onAction: CreateAccountActionHandler,
) {
  Column(verticalArrangement = Arrangement.spacedBy(CreateAccountDS.labelSpacing)) {
    Text(
      text = Strings.accountsCreateName,
      style = typography.bodyLarge,
      color = colors.pageText,
    )
    AktualTextField(
      modifier = Modifier.fillMaxWidth(),
      state = nameState,
      singleLine = true,
      placeholderText = Strings.accountsCreateNamePlaceholder,
      keyboardOptions = KeyboardOptions(capitalization = Words),
      supportingText =
        state.duplicateName?.let { name ->
          { Text(text = Strings.accountsCreateNameDuplicate(name), color = colors.errorText) }
        },
    )
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(CreateAccountDS.fieldSpacing),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = Strings.accountsCreateOffBudget,
        style = typography.bodyLarge,
        color = colors.pageText,
      )
      Text(
        text = Strings.accountsCreateOffBudgetHint,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }
    Switch(
      checked = state.offBudget,
      onCheckedChange = { onAction(SetOffBudget(it)) },
      colors = colors.switch(),
    )
  }

  Column(verticalArrangement = Arrangement.spacedBy(CreateAccountDS.labelSpacing)) {
    Text(
      text = Strings.accountsCreateBalance,
      style = typography.bodyLarge,
      color = colors.pageText,
    )
    Row(
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(CreateAccountDS.fieldSpacing),
    ) {
      AktualSlidingToggleButton(
        modifier = Modifier.width(CreateAccountDS.signToggleWidth),
        selected = state.isNegative,
        options = persistentListOf(false, true),
        onSelect = { onAction(SetNegative(it)) },
        string = { negative -> if (negative) MINUS else PLUS },
      )
      AmountField(modifier = Modifier.weight(1f), state = balanceState)
    }
    if (!state.isBalanceValid) {
      Text(
        text = Strings.accountsCreateBalanceInvalid,
        style = typography.bodySmall,
        color = colors.errorText,
      )
    }
  }

  PrimaryTextButtonWithLoading(
    modifier = Modifier.fillMaxWidth(),
    text = Strings.accountsCreateSubmit,
    isEnabled = state.canCreate,
    isLoading = state.isSaving,
    onClick = { onAction(Create) },
  )
}

private object CreateAccountDS {
  val contentPadding = 16.dp
  val fieldSpacing = 16.dp
  val labelSpacing = 4.dp
  val signToggleWidth = 96.dp
}

private const val PLUS = "+"
private const val MINUS = "−"

@PortraitPreview
@Composable
private fun PreviewCreateAccountScreen(
    @PreviewParameter(CreateAccountStateProvider::class) params: ColorParams<CreateAccountState>,
) = PreviewWithColorParams(params) { CreateAccountScaffold(state = this, onAction = {}) }

private class CreateAccountStateProvider :
  ColorParamsProvider<CreateAccountState>(
    CreateAccountState(),
    CreateAccountState(offBudget = true, isNegative = true, canCreate = true),
    CreateAccountState(duplicateName = "Savings", isBalanceValid = false),
    CreateAccountState(canCreate = true, isSaving = true),
  )

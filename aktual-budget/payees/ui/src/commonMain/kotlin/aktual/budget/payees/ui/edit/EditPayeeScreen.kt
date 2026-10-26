package aktual.budget.payees.ui.edit

import aktual.budget.model.Condition
import aktual.budget.model.ConditionSet
import aktual.budget.model.ConditionsFilter
import aktual.budget.model.PayeeId
import aktual.budget.payees.ui.list.PayeeChip
import aktual.budget.payees.ui.list.PayeesPreview
import aktual.budget.payees.ui.list.RuleCountChip
import aktual.budget.payees.ui.openRules
import aktual.budget.payees.vm.edit.EditPayeeState
import aktual.budget.payees.vm.edit.EditPayeeViewModel
import aktual.budget.payees.vm.edit.Editing
import aktual.budget.payees.vm.edit.Failure
import aktual.budget.payees.vm.edit.Loading
import aktual.budget.payees.vm.list.PayeeItem
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronLeft
import aktual.core.icons.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Save
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.SearchRulesNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.HazedTopBarState
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.TextFieldShape
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.switch
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.serialization.json.JsonPrimitive

@Composable
internal fun EditPayeeScreen(
  id: PayeeId,
  back: BackNavigator,
  editRule: EditRuleNavigator,
  searchRules: SearchRulesNavigator,
  toTransactions: TransactionsNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditPayeeViewModel = editPayeeViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
  val learnsCategories by viewModel.learnsCategories.collectAsStateWithLifecycle()
  val isLearningEnabled by viewModel.isLearningEnabled.collectAsStateWithLifecycle()
  val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsStateWithLifecycle()
  val canSave by viewModel.canSave.collectAsStateWithLifecycle()
  val isDuplicateName by viewModel.isDuplicateName.collectAsStateWithLifecycle()
  val saveError by viewModel.saveError.collectAsStateWithLifecycle()

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        FinishedSaving -> back()
      }
    }
  }

  EditPayeeScaffold(
    modifier = modifier,
    state = state,
    isFavorite = isFavorite,
    learnsCategories = learnsCategories,
    isLearningEnabled = isLearningEnabled,
    hasChanges = hasUnsavedChanges,
    canSave = canSave,
    isDuplicateName = isDuplicateName,
    saveError = saveError,
    onAction = { action ->
      when (action) {
        NavigateBack -> back()
        SavePayee -> viewModel.save()
        DismissSaveError -> viewModel.dismissSaveError()
        is SetName -> viewModel.setName(action.name)
        is SetFavorite -> viewModel.setFavorite(action.isFavorite)
        is SetLearnCategories -> viewModel.setLearnCategories(action.learn)
        is OpenRules -> openRules(action.payee, editRule, searchRules)
        is OpenTransactions -> toTransactions(action.title, payeeFilter(id))
      }
    },
  )
}

@Composable
private fun editPayeeViewModel(id: PayeeId) =
  assistedMetroViewModel<EditPayeeViewModel, EditPayeeViewModel.Factory>(key = id.value) {
    create(id)
  }

private fun payeeFilter(id: PayeeId) =
  ConditionsFilter(
    listOf(
      ConditionSet(
        listOf(Condition(field = Payee, operator = Is, type = Id, value = JsonPrimitive(id.value))),
      ),
    ),
  )

@Composable
private fun EditPayeeScaffold(
  state: EditPayeeState,
  isFavorite: Boolean,
  hasChanges: Boolean,
  canSave: Boolean,
  onAction: EditPayeeActionHandler,
  modifier: Modifier = Modifier,
  learnsCategories: Boolean = false,
  isLearningEnabled: Boolean = false,
  isDuplicateName: Boolean = false,
  saveError: String? = null,
) {
  val editing = state as? Editing
  val nameState = rememberTextFieldState()

  // seed the field once, when the payee finishes loading
  SideEffect(editing != null) {
    val loaded = editing ?: return@SideEffect
    nameState.setTextAndPlaceCursorAtEnd(loaded.payee.name)
  }

  LaunchedEffect(Unit) {
    snapshotFlow { nameState.text.toString() }.collect { onAction(SetName(it)) }
  }

  var showDiscardDialog by remember { mutableStateOf(false) }
  BackHandler(enabled = hasChanges) { showDiscardDialog = true }

  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      EditPayeeTopBar(
        editing = editing,
        hasChanges = hasChanges,
        saveEnabled = canSave,
        hazeState = hazeState,
        scrollState = scrollState,
        onAction = onAction,
        onRequestDiscard = { showDiscardDialog = true },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        Loading -> ShimmerEditPayee(modifier = Modifier.padding(innerPadding))

        is Editing ->
          EditPayeeContent(
            modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
            payee = state.payee,
            nameState = nameState,
            isFavorite = isFavorite,
            learnsCategories = learnsCategories,
            isLearningEnabled = isLearningEnabled,
            isDuplicateName = isDuplicateName,
            scrollState = scrollState,
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
            onAction = onAction,
          )

        is Failure ->
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.payeesEditFailurePrefix,
            reason = state.cause ?: Strings.payeesEditNotFound,
            action =
              FailureAction(
                text = { Strings.navBack },
                icon = AktualIcons.CheveronLeft,
                onClick = { onAction(NavigateBack) },
              ),
          )
      }

      if (showDiscardDialog) {
        DiscardChangesDialog(
          onDiscard = {
            showDiscardDialog = false
            onAction(NavigateBack)
          },
          onCancel = { showDiscardDialog = false },
        )
      }

      if (saveError != null) {
        SaveErrorDialog(message = saveError, onDismiss = { onAction(DismissSaveError) })
      }
    }
  }
}

@Composable
private fun EditPayeeTopBar(
  editing: Editing?,
  hasChanges: Boolean,
  saveEnabled: Boolean,
  hazeState: HazedTopBarState,
  scrollState: ScrollState,
  onAction: EditPayeeActionHandler,
  onRequestDiscard: () -> Unit,
) {
  TopAppBar(
    modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      if (hasChanges) {
        IconButton(onClick = onRequestDiscard) {
          Icon(
            imageVector = AktualIcons.Delete,
            contentDescription = Strings.payeesDiscardConfirm,
            tint = colors.errorText,
          )
        }
      } else {
        NavBackIconButton(onClick = { onAction(NavigateBack) })
      }
    },
    title = { if (editing != null) Text(text = Strings.payeesEditTitle) },
    actions = {
      if (editing != null && !editing.payee.isTransfer) {
        BareIconButton(
          imageVector = MaterialIcons.Save,
          contentDescription = Strings.payeesEditSave,
          enabled = saveEnabled,
          onClick = { onAction(SavePayee) },
        )
      }
    },
  )
}

@Composable
private fun DiscardChangesDialog(onDiscard: () -> Unit, onCancel: () -> Unit) {
  AktualAlertDialog(
    title = Strings.payeesDiscardTitle,
    highlight = colors.warningText,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(Strings.payeesDiscardCancel) }
      TextButton(onClick = onDiscard) {
        Text(Strings.payeesDiscardConfirm, color = colors.warningText)
      }
    },
    content = { Text(Strings.payeesDiscardMessage) },
  )
}

@Composable
private fun SaveErrorDialog(message: String, onDismiss: () -> Unit) {
  AktualAlertDialog(
    title = Strings.payeesSaveFailureTitle,
    highlight = colors.errorText,
    onDismissRequest = onDismiss,
    buttons = { TextButton(onClick = onDismiss) { Text(Strings.payeesSaveFailureDismiss) } },
    content = { Text(message) },
  )
}

@Composable
private fun EditPayeeContent(
  payee: PayeeItem,
  nameState: TextFieldState,
  isFavorite: Boolean,
  learnsCategories: Boolean,
  isLearningEnabled: Boolean,
  isDuplicateName: Boolean,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  onAction: EditPayeeActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(Dimens.ScreenPadding),
    verticalArrangement = Arrangement.spacedBy(EditPayeeDS.fieldSpacing),
  ) {
    Field(label = Strings.payeesEditNameLabel, required = !payee.isTransfer) {
      val supporting =
        when {
          payee.isTransfer -> Strings.payeesEditTransferHelp to colors.pageTextSubdued
          nameState.text.isBlank() -> Strings.payeesEditNameRequired to colors.errorText
          isDuplicateName -> Strings.payeesEditNameDuplicate to colors.warningText
          else -> null
        }
      AktualTextField(
        modifier = Modifier.fillMaxWidth(),
        state = nameState,
        singleLine = true,
        readOnly = payee.isTransfer,
        placeholderText = Strings.payeesEditNamePlaceholder,
        supportingText = supporting?.let { (text, color) -> { Text(text = text, color = color) } },
      )
    }

    SwitchRow(
      label = Strings.payeesFavorite,
      help = Strings.payeesEditFavoriteHelp,
      checked = isFavorite,
      onCheckedChange = { onAction(SetFavorite(it)) },
      enabled = !payee.isTransfer,
    )

    // Upstream only offers the per-payee switch while the global pref is on
    if (isLearningEnabled) {
      SwitchRow(
        label = Strings.payeesEditLearnLabel,
        help = Strings.payeesEditLearnHelp,
        checked = learnsCategories,
        onCheckedChange = { onAction(SetLearnCategories(it)) },
        enabled = !payee.isTransfer,
      )
    }

    Field(label = Strings.payeesEditRulesLabel) {
      RuleCountChip(numRules = payee.numRules, onClick = { onAction(OpenRules(payee)) })
    }

    Field(label = Strings.payeesEditTransactionsLabel) {
      val title = if (payee.isTransfer) Strings.payeesTransfer(payee.name) else payee.name
      PayeeChip(
        text = Plurals.payeesNumTransactions(payee.numTransactions, payee.numTransactions),
        onClick = { onAction(OpenTransactions(title)) },
      )
    }

    BottomSpacing()
  }
}

@Composable
private fun SwitchRow(
  label: String,
  help: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  enabled: Boolean,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(EditPayeeDS.switchSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing),
    ) {
      Text(text = label, style = typography.labelLarge, color = colors.pageText)
      Text(text = help, style = typography.bodySmall, color = colors.pageTextSubdued)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      colors = colors.switch(),
    )
  }
}

@Composable
private fun ShimmerEditPayee(modifier: Modifier = Modifier) {
  val bar = Modifier.background(colors.pageText, CardShape)
  val label =
    bar.size(width = EditPayeeDS.shimmerLabelWidth, height = EditPayeeDS.shimmerLabelHeight)
  val chip = bar.size(width = EditPayeeDS.shimmerChipWidth, height = EditPayeeDS.shimmerChipHeight)

  Column(
    modifier =
      modifier.fillMaxSize().padding(Dimens.ScreenPadding).shimmer(rememberShimmer(Window)),
    verticalArrangement = Arrangement.spacedBy(EditPayeeDS.fieldSpacing),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing)) {
      Box(label)
      Box(
        Modifier.background(colors.pageText, TextFieldShape)
          .fillMaxWidth()
          .height(EditPayeeDS.shimmerFieldHeight),
      )
    }

    repeat(times = 3) {
      Column(verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing)) {
        Box(label)
        Box(chip)
      }
    }
  }
}

@Composable
private inline fun Field(
  label: String,
  required: Boolean = false,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(EditPayeeDS.labelSpacing)) {
    val labelText =
      if (required) {
        buildAnnotatedString {
          append(label)
          withStyle(SpanStyle(color = colors.errorText)) { append(" *") }
        }
      } else {
        AnnotatedString(label)
      }
    Text(text = labelText, style = typography.labelLarge, color = colors.pageText)
    content()
  }
}

@PortraitPreview
@Composable
private fun PreviewEditPayeeScreen(
  @PreviewParameter(EditPayeeStateProvider::class) params: ColorParams<EditPayeeState>,
) =
  PreviewWithColorParams(params) {
    EditPayeeScaffold(
      state = this,
      isFavorite = (this as? Editing)?.payee?.isFavorite == true,
      learnsCategories = (this as? Editing)?.payee?.learnsCategories == true,
      isLearningEnabled = true,
      hasChanges = false,
      canSave = true,
      onAction = {},
    )
  }

private class EditPayeeStateProvider :
  ColorParamsProvider<EditPayeeState>(
    Loading,
    Editing(PayeesPreview.tesco),
    Editing(PayeesPreview.transfer),
    Failure(cause = null),
  )

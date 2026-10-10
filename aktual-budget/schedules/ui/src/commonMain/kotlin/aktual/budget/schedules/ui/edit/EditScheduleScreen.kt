package aktual.budget.schedules.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.model.ScheduleId
import aktual.budget.schedules.domain.ScheduleStatus
import aktual.budget.schedules.ui.list.ScheduleStatusBadge
import aktual.budget.schedules.vm.edit.EditScheduleError
import aktual.budget.schedules.vm.edit.EditScheduleState
import aktual.budget.schedules.vm.edit.EditScheduleViewModel
import aktual.budget.schedules.vm.edit.NamedEntity
import aktual.budget.schedules.vm.edit.ScheduleAmount
import aktual.budget.schedules.vm.edit.ScheduleDate
import aktual.budget.schedules.vm.edit.ScheduleForm
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.Clear
import aktual.core.icons.material.Delete
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Save
import aktual.core.icons.material.SaveAs
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.ListBottomSheet
import aktual.core.ui.LoadingScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.formatted
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.stringShort
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

@Composable
internal fun EditScheduleScreen(
  id: ScheduleId?,
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditScheduleViewModel = editScheduleViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val error by viewModel.error.collectAsStateWithLifecycle()

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        Created,
        Deleted -> back()
      }
    }
  }

  EditScheduleScaffold(
    modifier = modifier,
    state = state,
    error = error,
    onAction = { action ->
      when (action) {
        NavigateBack -> back()
        StartEditing -> viewModel.startEditing()
        StopEditing -> viewModel.stopEditing()
        SaveSchedule -> viewModel.save()
        DeleteSchedule -> viewModel.delete()
        DismissError -> viewModel.dismissError()
        is SetName -> viewModel.setName(action.name)
        is SetPayee -> viewModel.setPayee(action.id)
        is SetAccount -> viewModel.setAccount(action.id)
        is SetAmount -> viewModel.setAmount(action.amount)
        is SetDate -> viewModel.setDate(action.date)
        is SetPostsTransaction -> viewModel.setPostsTransaction(action.posts)
      }
    },
  )
}

@Composable
private fun editScheduleViewModel(id: ScheduleId?) =
  assistedMetroViewModel<EditScheduleViewModel, EditScheduleViewModel.Factory>(
    key = id.toString(),
  ) {
    create(id)
  }

private enum class Dialog {
  Delete,
  Discard,
}

@Composable
private fun EditScheduleScaffold(
  state: EditScheduleState,
  error: EditScheduleError?,
  onAction: EditScheduleActionHandler,
  modifier: Modifier = Modifier,
) {
  val loaded = state as? EditScheduleState.Loaded
  var dialog by remember { mutableStateOf<Dialog?>(null) }

  // Backing out of an edit drops back to viewing the schedule, checking first if that would lose
  // any changes
  fun onBack() {
    if (loaded != null && loaded.isEditing && loaded.hasChanges) {
      dialog = Discard
    } else if (loaded != null && loaded.isEditing && !loaded.isNew) {
      onAction(StopEditing)
    } else {
      onAction(NavigateBack)
    }
  }
  BackHandler(enabled = loaded?.isEditing == true) { onBack() }

  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = {
          if (loaded?.isEditing == true) {
            IconButton(onClick = { onBack() }) {
              Icon(
                imageVector = MaterialIcons.Clear,
                contentDescription = Strings.editScheduleStopEditing,
              )
            }
          } else {
            NavBackIconButton(onClick = { onBack() })
          }
        },
        title = {
          Text(text = toolbarTitle(loaded))
        },
        actions = {
          if (loaded != null) {
            TopBarActions(
              state = loaded,
              onAction = onAction,
              onDelete = { dialog = Dialog.Delete },
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        Loading -> LoadingScreen(modifier = Modifier.padding(innerPadding))

        is Failure ->
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.editScheduleFailureTitle,
            reason =
              when (state) {
                NotFound -> Strings.editScheduleFailureNotFound
                is Other -> state.reason
              },
            action =
              FailureAction(
                text = { Strings.navBack },
                icon = MaterialIcons.ArrowBack,
                onClick = { onAction(NavigateBack) },
              ),
          )

        is Loaded ->
          EditScheduleContent(
            modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
            state = state,
            scrollState = scrollState,
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
            onAction = onAction,
          )
      }

      EditScheduleDialog(
        dialog = dialog,
        isNew = loaded?.isNew == true,
        onAction = onAction,
        onDismiss = { dialog = null },
      )

      if (error != null) {
        ErrorDialog(error = error, onDismiss = { onAction(DismissError) })
      }
    }
  }
}

@Composable
private fun toolbarTitle(loaded: EditScheduleState.Loaded?): String =
  when {
    loaded?.isNew == true -> Strings.editScheduleToolbarCreate
    loaded?.isEditing == true -> Strings.editScheduleToolbarEdit
    else -> Strings.editScheduleToolbarView
  }

@Composable
private fun EditScheduleDialog(
  dialog: Dialog?,
  isNew: Boolean,
  onAction: EditScheduleActionHandler,
  onDismiss: () -> Unit,
) {
  when (dialog) {
    Delete ->
      ConfirmDialog(
        title = Strings.editScheduleDeleteTitle,
        message = Strings.editScheduleDeleteMessage,
        confirm = Strings.editScheduleDeleteConfirm,
        cancel = Strings.editScheduleDeleteCancel,
        highlight = colors.errorText,
        onConfirm = {
          onDismiss()
          onAction(DeleteSchedule)
        },
        onCancel = onDismiss,
      )

    Discard ->
      ConfirmDialog(
        title = Strings.editScheduleDiscardTitle,
        message = Strings.editScheduleDiscardMessage,
        confirm = Strings.editScheduleDiscardConfirm,
        cancel = Strings.editScheduleDiscardCancel,
        highlight = colors.warningText,
        onConfirm = {
          onDismiss()
          onAction(if (isNew) NavigateBack else StopEditing)
        },
        onCancel = onDismiss,
      )

    null -> Unit
  }
}

@Composable
private fun TopBarActions(
  state: EditScheduleState.Loaded,
  onAction: EditScheduleActionHandler,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier) {
    if (state.isEditing) {
      BareIconButton(
        imageVector = if (state.isNew) MaterialIcons.SaveAs else MaterialIcons.Save,
        contentDescription =
          if (state.isNew) Strings.editScheduleCreate else Strings.editScheduleSave,
        enabled = state.canSave,
        onClick = { onAction(SaveSchedule) },
      )
    } else {
      BareIconButton(
        imageVector = MaterialIcons.Edit,
        contentDescription = Strings.editScheduleEdit,
        enabled = !state.isWorking,
        onClick = { onAction(StartEditing) },
      )
      BareIconButton(
        imageVector = MaterialIcons.Delete,
        contentDescription = Strings.editScheduleDelete,
        enabled = !state.isWorking,
        onClick = onDelete,
      )
    }
  }
}

@Composable
private fun EditScheduleContent(
  state: EditScheduleState.Loaded,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  onAction: EditScheduleActionHandler,
  modifier: Modifier = Modifier,
) {
  var sheet by remember { mutableStateOf<SentencePart?>(null) }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(EditScheduleDS.contentPadding),
    verticalArrangement = Arrangement.spacedBy(EditScheduleDS.sectionSpacing),
  ) {
    if (state.isEditing) {
      NameField(name = state.form.name, onNameChange = { onAction(SetName(it)) })
    } else {
      Header(name = state.form.name, status = state.status)
    }

    ScheduleSentence(
      state = state,
      activePart = sheet,
      onClick = { part -> sheet = part },
    )

    if (state.isEditing) {
      Text(
        text = Strings.editScheduleHint,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )

      PostsTransactionRow(
        posts = state.form.postsTransaction,
        onChange = { onAction(SetPostsTransaction(it)) },
      )
    } else {
      Text(
        text =
          if (state.form.postsTransaction) {
            Strings.editSchedulePostsTransactionOn
          } else {
            Strings.editSchedulePostsTransactionOff
          },
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }

    UpcomingDates(dates = state.upcomingDates)

    BottomSpacing()
  }

  EditScheduleSheet(sheet = sheet, state = state, onAction = onAction, onDismiss = { sheet = null })
}

@Composable
private fun EditScheduleSheet(
  sheet: SentencePart?,
  state: EditScheduleState.Loaded,
  onAction: EditScheduleActionHandler,
  onDismiss: () -> Unit,
) {
  when (sheet) {
    SentencePart.Amount ->
      AmountSheet(
        amount = state.form.amount,
        onDismiss = onDismiss,
        onConfirm = { onAction(SetAmount(it)) },
      )

    When ->
      WhenSheet(
        date = state.form.date,
        onDismiss = onDismiss,
        onConfirm = { onAction(SetDate(it)) },
      )

    Payee ->
      EntitySheet(
        selected = state.form.payee ?: PayeeId(""),
        options = state.payees,
        onDismiss = onDismiss,
        onSelect = { onAction(SetPayee(it)) },
      )

    Account ->
      EntitySheet(
        selected = state.form.account ?: AccountId(""),
        options = state.accounts,
        onDismiss = onDismiss,
        onSelect = { onAction(SetAccount(it)) },
      )

    null -> Unit
  }
}

@Composable
private fun <T : Any> EntitySheet(
  selected: T,
  options: ImmutableList<NamedEntity<T>>,
  onDismiss: () -> Unit,
  onSelect: (T) -> Unit,
) {
  val sheetState = rememberBottomSheetState(Hidden)
  ListBottomSheet(
    value = options.firstOrNull { it.id == selected } ?: NamedEntity(selected, ""),
    options = options,
    onDismiss = onDismiss,
    onSelect = { onSelect(it.id) },
    sheetState = sheetState,
    string = { it.name },
    key = { it.id.toString() },
  )
}

@Composable
private fun Header(name: String, status: ScheduleStatus?, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.badgeSpacing),
  ) {
    Text(
      modifier = Modifier.weight(1f, fill = false),
      text = name.ifBlank { Strings.listSchedulesUnnamedSchedule },
      style = typography.headlineMedium,
      color = if (name.isBlank()) colors.pageTextSubdued else colors.pageText,
      maxLines = 2,
      overflow = Ellipsis,
    )

    if (status != null) {
      ScheduleStatusBadge(status)
    }
  }
}

@Composable
private fun NameField(name: String, onNameChange: (String) -> Unit, modifier: Modifier = Modifier) {
  val textState = rememberTextFieldState(initialText = name)
  val latestOnNameChange by rememberUpdatedState(onNameChange)

  // The view model owns the name, so follow it if it changes from elsewhere (e.g. a discard)
  SideEffect {
    if (textState.text.toString() != name) textState.setTextAndPlaceCursorAtEnd(name)
  }
  LaunchedEffect(textState) {
    snapshotFlow { textState.text.toString() }.collect { latestOnNameChange(it) }
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(EditScheduleDS.labelSpacing),
  ) {
    Text(text = Strings.editScheduleNameLabel, style = typography.labelLarge)
    AktualTextField(
      modifier = Modifier.fillMaxWidth(),
      state = textState,
      placeholderText = Strings.editScheduleNamePlaceholder,
      singleLine = true,
    )
  }
}

@Composable
private fun PostsTransactionRow(
  posts: Boolean,
  onChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.fieldSpacing),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = Strings.editSchedulePostsTransaction, style = typography.bodyLarge)
      Text(
        text = Strings.editSchedulePostsTransactionHint,
        style = typography.bodySmall,
        color = colors.pageTextSubdued,
      )
    }
    Switch(checked = posts, onCheckedChange = onChange, colors = colors.switch())
  }
}

@Composable
private fun UpcomingDates(dates: ImmutableList<LocalDate>, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(EditScheduleDS.labelSpacing),
  ) {
    Text(text = Strings.editScheduleUpcoming, style = typography.labelLarge)

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .background(colors.tableBackground, CardShape)
          .border(EditScheduleDS.hairline, colors.tableBorder, CardShape),
    ) {
      if (dates.isEmpty()) {
        Text(
          modifier = Modifier.padding(EditScheduleDS.rowPadding),
          text = Strings.editScheduleUpcomingNone,
          style = typography.bodyMedium,
          color = colors.pageTextSubdued,
        )
      }

      dates.forEachIndexed { index, date ->
        if (index > 0) HorizontalDivider(color = colors.tableBorder)
        Row(
          modifier = Modifier.fillMaxWidth().padding(EditScheduleDS.rowPadding),
          horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.fieldSpacing),
        ) {
          Text(
            text = date.dayOfWeek.stringShort(),
            style = typography.bodyMedium,
            color = colors.pageTextSubdued,
          )
          Text(text = date.formatted(), style = typography.bodyMedium, color = colors.pageText)
        }
      }
    }
  }
}

@Composable
private fun ConfirmDialog(
  title: String,
  message: String,
  confirm: String,
  cancel: String,
  highlight: Color,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
) {
  AktualAlertDialog(
    title = title,
    highlight = highlight,
    onDismissRequest = onCancel,
    buttons = {
      TextButton(onClick = onCancel) { Text(cancel) }
      TextButton(onClick = onConfirm) { Text(confirm, color = highlight) }
    },
    content = { Text(message) },
  )
}

@Composable
private fun ErrorDialog(error: EditScheduleError, onDismiss: () -> Unit) {
  AktualAlertDialog(
    title =
      if (error is Deleting) {
        Strings.editScheduleErrorDeleting
      } else {
        Strings.editScheduleErrorSaving
      },
    highlight = colors.errorText,
    onDismissRequest = onDismiss,
    buttons = { TextButton(onClick = onDismiss) { Text(Strings.editScheduleErrorDismiss) } },
    content = {
      Text(
        when (error) {
          is DuplicateName -> Strings.editScheduleErrorDuplicate(error.name)
          is Saving -> error.reason
          is Deleting -> error.reason
        },
      )
    },
  )
}

@PortraitPreview
@Composable
private fun PreviewEditScheduleScaffold(
  @PreviewParameter(EditScheduleStateProvider::class) params: ColoredParams<EditScheduleState>,
) =
  PreviewWithColoredParams(params) {
    EditScheduleScaffold(state = this, error = null, onAction = {})
  }

private val PreviewPayee = NamedEntity(PayeeId("landlord"), "Landlord Ltd")
private val PreviewAccount = NamedEntity(AccountId("current"), "Current account")

internal val PreviewLoaded =
  EditScheduleState.Loaded(
    form =
      ScheduleForm(
        name = "Rent",
        payee = PreviewPayee.id,
        account = PreviewAccount.id,
        amount = ScheduleAmount.Exactly(Amount(-120_000L)),
        date =
          ScheduleDate.Recurring(
            RecurConfig(
              frequency = Monthly,
              start = LocalDate(2025, 1, 1),
              interval = 1,
              skipWeekend = true,
              weekendSolveMode = Before,
              endMode = Never,
            ),
          ),
        postsTransaction = false,
      ),
    isNew = false,
    isEditing = false,
    hasChanges = false,
    isWorking = false,
    status = Due,
    payeeName = PreviewPayee.name,
    accountName = PreviewAccount.name,
    payees = persistentListOf(PreviewPayee),
    accounts = persistentListOf(PreviewAccount),
    upcomingDates =
      persistentListOf(LocalDate(2026, 10, 1), LocalDate(2026, 10, 30), LocalDate(2026, 12, 1)),
  )

private class EditScheduleStateProvider :
  ColoredParameterProvider<EditScheduleState>(
    Loading,
    EditScheduleState.Failure.NotFound,
    PreviewLoaded,
    PreviewLoaded.copy(isEditing = true, hasChanges = true),
    PreviewNew,
  )

private val PreviewOnceDate = LocalDate(2026, 11, 5)

private val PreviewNew =
  PreviewLoaded.copy(
    form =
      PreviewLoaded.form.copy(
        name = "",
        payee = null,
        amount = ScheduleAmount.Between(Amount(4_000L), Amount(6_000L)),
        date = ScheduleDate.Once(PreviewOnceDate),
      ),
    isNew = true,
    isEditing = true,
    status = null,
    payeeName = null,
    upcomingDates = persistentListOf(PreviewOnceDate),
  )

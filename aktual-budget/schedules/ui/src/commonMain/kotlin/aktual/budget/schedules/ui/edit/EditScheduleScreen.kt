package aktual.budget.schedules.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.model.ScheduleId
import aktual.budget.schedules.components.ScheduleStatusBadge
import aktual.budget.schedules.components.frequencyDescription
import aktual.budget.schedules.domain.ScheduleStatus
import aktual.budget.schedules.vm.edit.EditScheduleError
import aktual.budget.schedules.vm.edit.EditScheduleState
import aktual.budget.schedules.vm.edit.EditScheduleViewModel
import aktual.budget.schedules.vm.edit.NamedEntity
import aktual.budget.schedules.vm.edit.ScheduleAmount
import aktual.budget.schedules.vm.edit.ScheduleAmountType
import aktual.budget.schedules.vm.edit.ScheduleForm
import aktual.budget.schedules.vm.edit.isDeposit
import aktual.budget.schedules.vm.edit.type
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronLeft
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.Trash
import aktual.core.l10n.Plurals
import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.edit_schedule_saved
import aktual.core.nav.BackNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualSnackbar
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BackOrDiscardButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardColumn
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.DialogTextButton
import aktual.core.ui.Dimens
import aktual.core.ui.DiscardChangesDialog
import aktual.core.ui.EditFabs
import aktual.core.ui.FabClearance
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.ListBottomSheet
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.SectionLabel
import aktual.core.ui.SettingDivider
import aktual.core.ui.SettingRow
import aktual.core.ui.ShimmerBlock
import aktual.core.ui.ShimmerLine
import aktual.core.ui.TextFieldShape
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.isScrollingUp
import aktual.core.ui.redacted
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.showSuccessSnackbar
import aktual.core.ui.stringShort
import aktual.core.ui.switch
import aktual.core.ui.tabular
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import aktual.theme.Colors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString

@Composable
internal fun EditScheduleScreen(
  id: ScheduleId?,
  back: BackNavigator,
  viewTransactions: TransactionsNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditScheduleViewModel = editScheduleViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val error by viewModel.error.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        Created,
        Deleted -> back()
        Saved -> snackbarHostState.showSuccessSnackbar(getString(Res.string.edit_schedule_saved))
      }
    }
  }

  EditScheduleScaffold(
    modifier = modifier,
    state = state,
    error = error,
    snackbarHostState = snackbarHostState,
    onAction = { action ->
      when (action) {
        NavigateBack -> back()
        SaveSchedule -> viewModel.save()
        RevertSchedule -> viewModel.revert()
        DeleteSchedule -> viewModel.delete()
        DismissError -> viewModel.dismissError()
        is ViewTransactions -> if (id != null) viewTransactions(action.title, id)
        is SetName -> viewModel.setName(action.name)
        is SetPayee -> viewModel.setPayee(action.id)
        is SetAccount -> viewModel.setAccount(action.id)
        is SetAmount -> viewModel.setAmount(action.amount)
        is SetAmountType -> viewModel.setAmountType(action.type)
        is SetSkipWeekend -> viewModel.setSkipWeekend(action.skip)
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
  onAction: OnAction<EditScheduleAction>,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val loaded = state as? Loaded
  val hasChanges = loaded?.hasChanges == true
  var dialog by remember { mutableStateOf<Dialog?>(null) }
  BackHandler(enabled = hasChanges) { dialog = Discard }

  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize().imePadding(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = {
          BackOrDiscardButton(
            hasChanges = hasChanges,
            discardText = Strings.editScheduleDiscardConfirm,
            onBack = { onAction(NavigateBack) },
            onDiscard = { dialog = Discard },
          )
        },
        title = {
          Text(
            if (loaded?.isNew == true) {
              Strings.editScheduleToolbarCreate
            } else {
              Strings.editScheduleToolbarEdit
            },
          )
        },
        actions = {
          if (loaded != null && !loaded.isNew) {
            MoreMenu(isEnabled = !loaded.isWorking, onDelete = { dialog = Delete })
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        Loading -> ShimmerSchedule(modifier = Modifier.padding(innerPadding))

        is Failure ->
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.editScheduleFailureTitle,
            reason =
              when (state) {
                NotFound -> Strings.editScheduleFailureNotFound
                is OtherFailure -> state.reason
              },
            action =
              FailureAction(
                text = { Strings.navBack },
                icon = AktualIcons.CheveronLeft,
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

      // Not in the scaffold's slots, which put the snackbar above the save button so it jumps down
      // when the button hides after saving
      Column(
        modifier = Modifier.align(BottomEnd).fillMaxWidth(),
        horizontalAlignment = Alignment.End,
      ) {
        SaveDiscardFabs(
          state = loaded,
          scrollState = scrollState,
          onAction = onAction,
        )
        SnackbarHost(
          hostState = snackbarHostState,
          modifier = Modifier.align(CenterHorizontally),
          snackbar = ::AktualSnackbar,
        )
        BottomSpacing()
      }

      EditScheduleDialog(dialog = dialog, onAction = onAction, onDismiss = { dialog = null })

      if (error != null) {
        ErrorDialog(error = error, onDismiss = { onAction(DismissError) })
      }
    }
  }
}

// New schedules always show save, since there's nothing saved to compare against
@Composable
private fun SaveDiscardFabs(
  state: EditScheduleState.Loaded?,
  scrollState: ScrollState,
  onAction: OnAction<EditScheduleAction>,
) {
  EditFabs(
    saveText = if (state?.isNew == true) Strings.editScheduleCreate else Strings.editScheduleSave,
    discardText = Strings.editScheduleDiscardConfirm,
    onSave = { onAction(SaveSchedule) },
    onDiscard = { onAction(RevertSchedule) },
    isSaveVisible = state != null && (state.isNew || state.hasChanges),
    isDiscardVisible = state?.hasChanges == true,
    canSave = state?.canSave == true,
    isSaving = state?.isWorking == true,
    isExpanded = scrollState.isScrollingUp(),
  )
}

@Composable
private fun MoreMenu(isEnabled: Boolean, onDelete: () -> Unit) = Box {
  var showMenu by remember { mutableStateOf(false) }

  AktualIconButton(
    onClick = { showMenu = true },
    isEnabled = isEnabled,
    imageVector = AktualIcons.DotsHorizontalTriple,
    contentDescription = Strings.editScheduleMenu,
  )

  AktualDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
    val string = Strings.editScheduleDelete
    val red = colors.errorText
    AktualDropdownMenuItem(
      text = { Text(string, color = red) },
      onClick = {
        showMenu = false
        onDelete()
      },
      leadingIcon = {
        Icon(imageVector = AktualIcons.Trash, contentDescription = string, tint = red)
      },
    )
  }
}

@Composable
private fun EditScheduleDialog(
  dialog: Dialog?,
  onAction: OnAction<EditScheduleAction>,
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
      DiscardChangesDialog(
        title = Strings.editScheduleDiscardTitle,
        message = Strings.editScheduleDiscardMessage,
        confirm = Strings.editScheduleDiscardConfirm,
        cancel = Strings.editScheduleDiscardCancel,
        onDiscard = {
          onDismiss()
          onAction(NavigateBack)
        },
        onCancel = onDismiss,
      )

    null -> Unit
  }
}

/** Keep in sync with [ShimmerSchedule] */
@Composable
private fun EditScheduleContent(
  state: EditScheduleState.Loaded,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  onAction: OnAction<EditScheduleAction>,
  modifier: Modifier = Modifier,
) {
  var sheet by remember { mutableStateOf<ScheduleSheet?>(null) }
  val form = state.form
  // Everything on a new schedule is new, so only edits to a saved one are marked
  val changes = if (state.isNew) NoChanges else changesBetween(form, state.saved)

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScrollWithBar(scrollState)
        .padding(contentPadding)
        .padding(Dimens.ScreenPadding),
  ) {
    NameField(name = form.name, onNameChange = { onAction(SetName(it)) })

    if (!state.isNew) {
      StatusRow(status = state.status, nextDate = state.upcomingDates.firstOrNull())
    }

    TransactionSection(state, changes, onAction, onOpenSheet = { sheet = it })
    ScheduleSection(form, changes, onAction, onOpenSheet = { sheet = it })

    Section(label = Strings.editScheduleUpcoming) { UpcomingDates(dates = state.upcomingDates) }

    if (!state.isNew) {
      val title = form.name.ifBlank { Strings.listSchedulesUnnamedSchedule }
      Card(modifier = Modifier.padding(top = EditScheduleDS.sectionSpacing)) {
        SettingRow(onClick = { onAction(ViewTransactions(title)) }) {
          Text(
            text = Strings.editScheduleViewTransactions,
            style = typography.bodyLarge,
            color = colors.tableText,
          )
        }
      }
    }

    // Room to scroll the last item out from under the save button
    Spacer(modifier = Modifier.height(FabClearance))
    BottomSpacing()
  }

  EditScheduleSheet(sheet = sheet, state = state, onAction = onAction, onDismiss = { sheet = null })
}

@Composable
private fun TransactionSection(
  state: EditScheduleState.Loaded,
  changes: Changes,
  onAction: OnAction<EditScheduleAction>,
  onOpenSheet: (ScheduleSheet) -> Unit,
) =
  Section(label = Strings.editScheduleSectionTransaction) {
    ValueRow(
      label = Strings.editSchedulePayee,
      value = state.payeeName ?: Strings.editSchedulePayeeNone,
      isChanged = changes.payee,
      onClick = { onOpenSheet(Payee) },
    )
    SettingDivider()
    ValueRow(
      label = Strings.editScheduleAccount,
      value = state.accountName ?: Strings.editScheduleAccountNone,
      isChanged = changes.account,
      onClick = { onOpenSheet(Account) },
    )
    SettingDivider()
    AmountTypeRow(
      type = state.form.amount.type,
      isChanged = changes.amountType,
      onSelect = { onAction(SetAmountType(it)) },
    )
    SettingDivider()
    ValueRow(
      label = Strings.editScheduleAmountTitle,
      value = state.form.amount.text(),
      valueColor = state.form.amount.color(),
      isChanged = changes.amount,
      onClick = { onOpenSheet(ScheduleSheet.Amount) },
    )
  }

@Composable
private fun ScheduleSection(
  form: ScheduleForm,
  changes: Changes,
  onAction: OnAction<EditScheduleAction>,
  onOpenSheet: (ScheduleSheet) -> Unit,
) =
  Section(label = Strings.editScheduleSectionSchedule) {
    val recurring = form.date as? Recurring
    ValueRow(
      label = Strings.editScheduleWhenRepeats,
      value = recurring?.config?.frequencyDescription() ?: Strings.editScheduleRepeatsNever,
      isChanged = changes.repeats,
      onClick = { onOpenSheet(When) },
    )
    SettingDivider()
    ValueRow(
      label =
        if (recurring == null) Strings.editScheduleWhenDate else Strings.editScheduleWhenStarts,
      value = form.date.start.formatted(),
      isChanged = changes.start,
      onClick = { onOpenSheet(When) },
    )
    if (recurring != null) {
      SettingDivider()
      ValueRow(
        label = Strings.editScheduleEnds,
        value = recurring.config.endText(),
        isChanged = changes.ends,
        onClick = { onOpenSheet(When) },
      )
      SettingDivider()
      SwitchRow(
        label = Strings.editScheduleWeekendMove,
        isChecked = recurring.config.skipWeekend == true,
        isChanged = changes.skipWeekend,
        onChange = { onAction(SetSkipWeekend(it)) },
      )
    }
    SettingDivider()
    SwitchRow(
      label = Strings.editSchedulePostsTransaction,
      isChecked = form.postsTransaction,
      isChanged = changes.postsTransaction,
      onChange = { onAction(SetPostsTransaction(it)) },
    )
  }

// Which rows differ from the saved schedule
@Immutable
private data class Changes(
  val payee: Boolean = false,
  val account: Boolean = false,
  val amountType: Boolean = false,
  val amount: Boolean = false,
  val repeats: Boolean = false,
  val start: Boolean = false,
  val ends: Boolean = false,
  val skipWeekend: Boolean = false,
  val postsTransaction: Boolean = false,
)

private val NoChanges = Changes()

private fun changesBetween(form: ScheduleForm, saved: ScheduleForm): Changes {
  val config = (form.date as? Recurring)?.config
  val savedConfig = (saved.date as? Recurring)?.config
  return Changes(
    payee = form.payee != saved.payee,
    account = form.account != saved.account,
    amountType = form.amount.type != saved.amount.type,
    amount = form.amount.values != saved.amount.values,
    repeats = config?.repeatKey != savedConfig?.repeatKey,
    start = form.date.start != saved.date.start,
    ends = config?.endKey != savedConfig?.endKey,
    skipWeekend = config?.skipWeekend != savedConfig?.skipWeekend,
    postsTransaction = form.postsTransaction != saved.postsTransaction,
  )
}

private val ScheduleAmount.values: List<Amount>
  get() =
    when (this) {
      is Exactly -> listOf(amount)
      is Approximately -> listOf(amount)
      is Between -> listOf(from, to)
    }

private val RecurConfig.repeatKey: List<Any?>
  get() = listOf(frequency, interval ?: 1, patterns)

// Unknown modes are shown as never ending, so they compare the same
private val RecurConfig.endKey: List<Any?>
  get() =
    when (endMode) {
      AfterNOccurrences -> listOf(endMode, endOccurrences)
      OnDate -> listOf(endMode, endDate)
      Never,
      Unknown,
      null -> emptyList()
    }

@Composable
private fun NameField(name: String, onNameChange: (String) -> Unit, modifier: Modifier = Modifier) {
  val textState = rememberTextFieldState(initialText = name)
  val latestOnNameChange by rememberUpdatedState(onNameChange)
  // The view model owns the name, so follow it if it changes from elsewhere
  SideEffect {
    if (textState.text.toString() != name) textState.setTextAndPlaceCursorAtEnd(name)
  }
  LaunchedEffect(textState) {
    snapshotFlow { textState.text.toString() }.collect { latestOnNameChange(it) }
  }

  AktualTextField(
    modifier = modifier.fillMaxWidth(),
    state = textState,
    placeholderText = Strings.editScheduleNamePlaceholder,
    singleLine = true,
    keyboardOptions = KeyboardOptions(capitalization = Sentences),
  )
}

@Composable
private fun StatusRow(
  status: ScheduleStatus?,
  nextDate: LocalDate?,
  modifier: Modifier = Modifier,
) {
  if (status == null && nextDate == null) return
  Row(
    modifier = modifier.padding(top = EditScheduleDS.fieldSpacing),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.badgeSpacing),
  ) {
    status?.let { ScheduleStatusBadge(it) }
    nextDate?.let { date ->
      Text(
        text = Strings.editScheduleNextDue(date.formatted()),
        style = typography.bodyMedium,
        color = colors.textTertiary,
      )
    }
  }
}

@Composable
private fun Section(label: String, content: @Composable ColumnScope.() -> Unit) =
  Column(modifier = Modifier.padding(top = EditScheduleDS.sectionSpacing)) {
    SectionLabel(label)
    Card(content = content)
  }

@Composable
private fun Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
  Column(
    modifier = modifier.fillMaxWidth().clip(CardShape).background(colors.background),
    content = content,
  )

@Composable
private fun ValueRow(
  label: String,
  value: String,
  isChanged: Boolean,
  onClick: () -> Unit,
  valueColor: Color = colors.tableHeaderText,
) =
  SettingRow(modifier = Modifier.changedEdge(colors, isChanged), onClick = onClick) {
    Text(text = label, style = typography.bodyLarge, color = colors.tableText)
    Text(
      modifier = Modifier.weight(1f),
      text = value.redacted(),
      style = typography.bodyMedium,
      color = valueColor,
      textAlign = End,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }

@Composable
private fun SwitchRow(
  label: String,
  isChecked: Boolean,
  isChanged: Boolean,
  onChange: (Boolean) -> Unit,
) =
  SettingRow(
    modifier = Modifier.changedEdge(colors, isChanged),
    onClick = { onChange(!isChecked) },
    showChevron = false,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = label,
      style = typography.bodyLarge,
      color = colors.tableText,
    )
    Switch(checked = isChecked, onCheckedChange = onChange, colors = colors.switch())
  }

@Composable
private fun AmountTypeRow(
  type: ScheduleAmountType,
  isChanged: Boolean,
  onSelect: (ScheduleAmountType) -> Unit,
) {
  AktualSlidingToggleButton(
    modifier =
      Modifier.changedEdge(colors, isChanged).padding(EditScheduleDS.rowPadding).fillMaxWidth(),
    selected = type,
    options = ScheduleAmountType.entries.toImmutableList(),
    onSelect = onSelect,
    string = { it.string() },
  )
}

private fun Modifier.changedEdge(colors: Colors, isChanged: Boolean) = drawBehind {
  if (isChanged) {
    drawRect(
      color = colors.pageTextLinkLight,
      size = Size(EditScheduleDS.changedEdge.toPx(), size.height),
    )
  }
}

@Composable
internal fun ScheduleAmountType.string(): String =
  when (this) {
    Exactly -> Strings.editScheduleAmountOpExactly
    Approximately -> Strings.editScheduleAmountOpApprox
    Between -> Strings.editScheduleAmountOpBetween
  }

@Composable
private fun ScheduleAmount.text(): String =
  when (this) {
    is Exactly -> amount.formattedString(includeSign = true)
    is Approximately -> amount.formattedString(includeSign = true)
    is Between ->
      Strings.editScheduleAmountRange(
        from.formattedString(includeSign = true),
        to.formattedString(includeSign = true),
      )
  }

@Composable
@ReadOnlyComposable
private fun ScheduleAmount.color(): Color =
  when {
    values.all { it.toLong() == 0L } -> colors.tableHeaderText
    isDeposit -> colors.numberPositive
    else -> colors.numberNegative
  }

@Composable
private fun RecurConfig.endText(): String =
  when (endMode) {
    AfterNOccurrences -> {
      val count = endOccurrences ?: 1
      Plurals.editScheduleEndsAfterCount(count, count)
    }
    OnDate -> endDate?.formatted() ?: Strings.editScheduleEndsNever
    Never,
    Unknown,
    null -> Strings.editScheduleEndsNever
  }

// Stands in for the content while the schedule loads
/** Keep in sync with [EditScheduleContent] */
@Composable
private fun ShimmerSchedule(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(Window)

  Column(modifier = modifier.fillMaxSize().padding(Dimens.ScreenPadding)) {
    Column(modifier = Modifier.shimmer(shimmer)) {
      ShimmerBlock(
        modifier = Modifier.fillMaxWidth().height(EditScheduleDS.shimmerNameHeight),
        shape = TextFieldShape,
      )
      Row(
        modifier = Modifier.padding(top = EditScheduleDS.fieldSpacing),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.badgeSpacing),
      ) {
        ShimmerBlock(
          Modifier.size(
            width = EditScheduleDS.shimmerBadgeWidth,
            height = EditScheduleDS.shimmerBadgeHeight,
          ),
        )
        ShimmerLine(
          modifier = Modifier.width(EditScheduleDS.shimmerValueWidth),
          style = typography.bodyMedium,
        )
      }
    }

    ShimmerSection(rows = SHIMMER_TRANSACTION_ROWS, shimmer = shimmer)
    ShimmerSection(rows = SHIMMER_SCHEDULE_ROWS, shimmer = shimmer)
    ShimmerSection(rows = SHIMMER_UPCOMING_ROWS, shimmer = shimmer)
  }
}

@Composable
private fun ShimmerSection(rows: Int, shimmer: Shimmer, modifier: Modifier = Modifier) =
  Column(modifier = modifier.padding(top = EditScheduleDS.sectionSpacing)) {
    ShimmerLine(
      modifier =
        Modifier.shimmer(shimmer)
          .padding(EditScheduleDS.sectionLabelPadding)
          .width(EditScheduleDS.shimmerLabelWidth),
      style = typography.labelMedium,
    )
    Card {
      repeat(rows) { index ->
        if (index > 0) SettingDivider()
        SettingRow(onClick = null) {
          Row(
            modifier = Modifier.weight(1f).shimmer(shimmer),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            ShimmerLine(
              modifier = Modifier.width(EditScheduleDS.shimmerLabelWidth),
              style = typography.bodyLarge,
            )
            ShimmerLine(
              modifier = Modifier.width(EditScheduleDS.shimmerValueWidth),
              style = typography.bodyMedium,
            )
          }
        }
      }
    }
  }

private const val SHIMMER_TRANSACTION_ROWS = 4
private const val SHIMMER_SCHEDULE_ROWS = 5
private const val SHIMMER_UPCOMING_ROWS = 3

@Composable
private fun EditScheduleSheet(
  sheet: ScheduleSheet?,
  state: EditScheduleState.Loaded,
  onAction: OnAction<EditScheduleAction>,
  onDismiss: () -> Unit,
) {
  when (sheet) {
    ScheduleSheet.Amount ->
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
        searchPlaceholder = Strings.searchPayees,
        onDismiss = onDismiss,
        onSelect = { onAction(SetPayee(it)) },
      )

    Account ->
      EntitySheet(
        selected = state.form.account ?: AccountId(""),
        options = state.accounts,
        searchPlaceholder = Strings.searchAccounts,
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
  searchPlaceholder: String,
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
    searchPlaceholder = searchPlaceholder,
  )
}

@Composable
private fun UpcomingDates(dates: ImmutableList<LocalDate>, modifier: Modifier = Modifier) =
  CardColumn(modifier = modifier.fillMaxWidth(), contentPadding = Zero) {
    if (dates.isEmpty()) {
      Text(
        modifier = Modifier.padding(EditScheduleDS.rowPadding),
        text = Strings.editScheduleUpcomingNone,
        style = typography.bodyMedium,
        color = colors.tableText,
      )
    }

    dates.fastForEachIndexed { index, date ->
      if (index > 0) SettingDivider()
      Row(
        modifier = Modifier.fillMaxWidth().padding(EditScheduleDS.rowPadding),
        horizontalArrangement = Arrangement.spacedBy(EditScheduleDS.fieldSpacing),
      ) {
        Text(
          modifier = Modifier.width(EditScheduleDS.dayWidth),
          text = date.dayOfWeek.stringShort(),
          style = typography.bodyMedium.tabular(),
          color = colors.tableHeaderText,
        )
        Text(
          text = date.formatted(),
          style = typography.bodyMedium.tabular(),
          color = colors.tableText,
        )
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
      DialogTextButton(text = cancel, onClick = onCancel)
      DialogTextButton(kind = Primary, text = confirm, onClick = onConfirm)
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
    buttons = { DialogTextButton(text = Strings.editScheduleErrorDismiss, onClick = onDismiss) },
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
  @PreviewParameter(EditScheduleStateProvider::class) params: ColorParams<EditScheduleState>,
) =
  PreviewWithColorParams(params) {
    EditScheduleScaffold(state = this, error = null, onAction = {})
  }

private class EditScheduleStateProvider :
  ColorParamsProvider<EditScheduleState>(
    PreviewLoaded,
    PreviewDirty,
    PreviewDirty.copy(isWorking = true),
    PreviewNew,
    PreviewOnce,
    Loading,
    NotFound,
  )

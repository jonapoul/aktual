package aktual.budget.rules.ui.edit

import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.ConditionEntityLists
import aktual.budget.rules.domain.Rule
import aktual.budget.rules.domain.hasUnknownValues
import aktual.budget.rules.domain.newCondition
import aktual.budget.rules.domain.nextAction
import aktual.budget.rules.domain.splitIndex
import aktual.budget.rules.ui.DeleteRuleDialog
import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.ui.PreviewCondition2
import aktual.budget.rules.ui.PreviewRule1
import aktual.budget.rules.vm.edit.EditRuleState
import aktual.budget.rules.vm.edit.EditRuleState.Failure
import aktual.budget.rules.vm.edit.EditRuleState.Loading
import aktual.budget.rules.vm.edit.EditRuleState.Success
import aktual.budget.rules.vm.edit.EditRuleViewModel
import aktual.budget.rules.vm.edit.RuleMatchesState
import aktual.budget.schedules.domain.Schedule
import aktual.core.icons.AktualIcons
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.ViewShow
import aktual.core.icons.material.DeleteForever
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.edit_rule_failed_saving
import aktual.core.l10n.edit_rule_matches_applied
import aktual.core.l10n.edit_rule_matches_apply_failed
import aktual.core.l10n.edit_rule_saved
import aktual.core.nav.BackNavigator
import aktual.core.nav.EditScheduleNavigator
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualSnackbar
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BackOrDiscardButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.DiscardChangesDialog
import aktual.core.ui.EditFabs
import aktual.core.ui.FabClearance
import aktual.core.ui.FailureScreen
import aktual.core.ui.HostedSheet
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.ShimmerBlock
import aktual.core.ui.ShimmerLine
import aktual.core.ui.WarningBanner
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.isScrollingUp
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.showErrorSnackbar
import aktual.core.ui.showSuccessSnackbar
import aktual.core.ui.transparentTopAppBarColors
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.ShimmerBounds.Window
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

@Composable
internal fun EditRuleScreen(
  id: RuleId?,
  payee: PayeeId?,
  back: BackNavigator,
  editSchedule: EditScheduleNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditRuleViewModel = metroViewModel(id, payee),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val conditionEntities by viewModel.conditionEntities.collectAsStateWithLifecycle()
  val schedules by viewModel.schedules.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        // Like upstream, a new rule goes back to where it was created from
        Created,
        DeletedRule -> back()
        is AppliedActions ->
          snackbarHostState.showSnackbar(
            getPluralString(Res.plurals.edit_rule_matches_applied, event.count, event.count),
          )
        ApplyFailed ->
          snackbarHostState.showErrorSnackbar(getString(Res.string.edit_rule_matches_apply_failed))
        Saved -> snackbarHostState.showSuccessSnackbar(getString(Res.string.edit_rule_saved))
        is SaveFailed ->
          snackbarHostState.showErrorSnackbar(
            getString(Res.string.edit_rule_failed_saving, event.reason),
          )
      }
    }
  }

  CompositionLocalProvider(LocalNameFetcher provides viewModel.nameFetcher) {
    EditRuleScaffold(
      modifier = modifier,
      mode = if (id == null) Create else Edit,
      state = state,
      conditionEntities = conditionEntities,
      schedules = schedules,
      matches = viewModel.matches,
      snackbarHostState = snackbarHostState,
      onAction = { action ->
        when (action) {
          NavBack -> back()
          is OpenSchedule -> editSchedule(action.id)
          is ViewModelAction -> viewModel.handle(action)
        }
      },
    )
  }
}

private fun EditRuleViewModel.handle(action: ViewModelAction) {
  when (action) {
    Delete -> delete()
    Save -> save()
    Revert -> revert()
    is SetStage -> setStage(action.value)
    is SetConditionsOp -> setConditionsOp(action.value)
    is SetCondition -> setCondition(action.key, action.condition)
    is AddCondition -> addCondition(action.condition)
    is RemoveCondition -> removeCondition(action.key)
    is SetAction -> setAction(action.key, action.action)
    is AddAction -> addAction(action.action)
    is RemoveAction -> removeAction(action.key)
    AddSplit -> addSplit()
    is RemoveSplit -> removeSplit(action.splitIndex)
    ApplyActions -> applyActions()
    is ToggleMatch -> toggleMatch(action.id)
    ToggleAllMatches -> toggleAllMatches()
  }
}

@Composable
private fun metroViewModel(id: RuleId?, payee: PayeeId?) =
  assistedMetroViewModel<EditRuleViewModel, EditRuleViewModel.Factory>(key = "$id-$payee") {
    create(id, payee)
  }

internal enum class Mode {
  Create,
  Edit,
}

@Composable
private fun EditRuleScaffold(
  state: EditRuleState,
  mode: Mode,
  conditionEntities: ConditionEntities?,
  schedules: ImmutableList<Schedule>?,
  matches: StateFlow<RuleMatchesState>,
  onAction: OnAction<EditRuleAction>,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  var showMatches by rememberSaveable { mutableStateOf(false) }
  var showDiscardDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  val hasChanges = state is Success && state.hasChanges

  BackHandler(enabled = hasChanges) { showDiscardDialog = true }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      EditRuleTopBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        state = state,
        mode = mode,
        onBack = { onAction(NavBack) },
        onDiscard = { showDiscardDialog = true },
        onShowMatches = { showMatches = true },
        onDelete = { showDeleteDialog = true },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      val contentModifier = Modifier.hazedTopBarContent(hazeState, innerPadding)
      val contentPadding =
        PaddingValues(
          start = Dimens.Huge,
          end = Dimens.Huge,
          top =
            hazedTopBarContentPadding(hazeState, innerPadding).calculateTopPadding() + Dimens.Huge,
        )

      when (state) {
        is Failure ->
          FailureScreen(
            modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
            title = Strings.editRuleFailedTitle,
            reason = state.reason,
            action = null,
          )

        Loading -> LoadingContent(contentModifier.padding(contentPadding))

        is Success ->
          LoadedContent(
            modifier = contentModifier,
            listState = listState,
            contentPadding = contentPadding,
            rule = state.rule,
            conditionKeys = state.conditionKeys,
            actionKeys = state.actionKeys,
            conditionEntities = conditionEntities,
            schedules = schedules,
            isScheduleRule = state.schedule != null,
            isEnabled = !state.isWorking,
            onAction = onAction,
          )
      }

      BottomBar(
        state = state,
        mode = mode,
        listState = listState,
        snackbarHostState = snackbarHostState,
        onAction = onAction,
      )
    }
  }

  if (showMatches && state is Success) {
    HostedSheet(onDismiss = { showMatches = false }) {
      // Collected only while shown, so the matches aren't searched for while the sheet is closed
      val matchesState by matches.collectAsStateWithLifecycle()
      MatchesSheetContent(state = matchesState, isEnabled = !state.isWorking, onAction = onAction)
    }
  }

  if (showDeleteDialog) {
    DeleteRuleDialog(
      onConfirm = {
        showDeleteDialog = false
        onAction(Delete)
      },
      onDismiss = { showDeleteDialog = false },
    )
  }

  if (showDiscardDialog) {
    DiscardChangesDialog(
      title = Strings.editRuleDiscardTitle,
      message = Strings.editRuleDiscardMessage,
      confirm = Strings.editRuleDiscardConfirm,
      cancel = Strings.editRuleDiscardCancel,
      onDiscard = {
        showDiscardDialog = false
        onAction(NavBack)
      },
      onCancel = { showDiscardDialog = false },
    )
  }
}

@Composable
private fun EditRuleTopBar(
  state: EditRuleState,
  mode: Mode,
  onBack: () -> Unit,
  onDiscard: () -> Unit,
  onShowMatches: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TopAppBar(
    modifier = modifier,
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      BackOrDiscardButton(
        hasChanges = state is Success && state.hasChanges,
        discardText = Strings.editRuleDiscardConfirm,
        onBack = onBack,
        onDiscard = onDiscard,
      )
    },
    title = {
      Text(if (mode == Create) Strings.editRuleToolbarCreate else Strings.editRuleToolbarEdit)
    },
    actions = {
      MoreMenu(
        canDelete = mode == Edit && state is Success && state.canDelete,
        isEnabled = state is Success && !state.isWorking,
        onShowMatches = onShowMatches,
        onDelete = onDelete,
      )
    },
  )
}

// Not in the scaffold's slots, which put the snackbar above the save button so it jumps down when
// the button hides after saving
@Composable
private fun BoxScope.BottomBar(
  state: EditRuleState,
  mode: Mode,
  listState: LazyListState,
  snackbarHostState: SnackbarHostState,
  onAction: OnAction<EditRuleAction>,
) =
  Column(modifier = Modifier.align(BottomEnd).fillMaxWidth(), horizontalAlignment = End) {
    val success = state as? Success
    EditFabs(
      saveText = if (mode == Create) Strings.editRuleCreate else Strings.editRuleSave,
      discardText = Strings.editRuleDiscardConfirm,
      onSave = { onAction(Save) },
      onDiscard = { onAction(Revert) },
      isSaveVisible = success?.hasChanges == true,
      canSave = success?.canSave == true,
      isSaving = success?.isWorking == true,
      isExpanded = listState.isScrollingUp(),
    )
    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier.align(CenterHorizontally),
      snackbar = ::AktualSnackbar,
    )
    BottomSpacing()
  }

@Composable
private fun MoreMenu(
  canDelete: Boolean,
  isEnabled: Boolean,
  onShowMatches: () -> Unit,
  onDelete: () -> Unit,
) = Box {
  var showMenu by remember { mutableStateOf(false) }

  AktualIconButton(
    onClick = { showMenu = true },
    isEnabled = isEnabled,
    imageVector = AktualIcons.DotsHorizontalTriple,
    contentDescription = Strings.editRuleMenu,
  )

  AktualDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
    val matches = Strings.editRuleMatchesShow
    AktualDropdownMenuItem(
      text = { Text(matches) },
      onClick = {
        showMenu = false
        onShowMatches()
      },
      leadingIcon = { Icon(imageVector = AktualIcons.ViewShow, contentDescription = matches) },
    )

    if (canDelete) {
      val string = Strings.editRuleActionDelete
      val red = colors.errorText
      AktualDropdownMenuItem(
        text = { Text(string, color = red) },
        onClick = {
          showMenu = false
          onDelete()
        },
        leadingIcon = {
          Icon(imageVector = MaterialIcons.DeleteForever, contentDescription = string, tint = red)
        },
      )
    }
  }
}

private val Failure.reason: String
  @Composable
  get() =
    when (this) {
      NotFound -> Strings.editRuleFailedNoMatch
      is OtherFailure -> Strings.editRuleFailedOther(reason)
    }

/** Keep in sync with [LoadedContent] */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(Window)
  Column(modifier = modifier.shimmer(shimmer)) {
    TimelineRow(hasRail = true, node = { DotNode() }, isEndOfStep = true) {
      ShimmerHeading(fraction = 0.3f)
      ShimmerBlock(Modifier.fillMaxWidth().height(36.dp))
    }
    TimelineRow(hasRail = true, node = { ShimmerNode() }) { ShimmerHeading(fraction = 0.4f) }
    TimelineRow(hasRail = true) { ShimmerCard() }
    TimelineRow(hasRail = true, isEndOfStep = true) { ShimmerCard() }
    TimelineRow(hasRail = false, node = { ShimmerNode() }) { ShimmerHeading(fraction = 0.2f) }
    TimelineRow(hasRail = false) { ShimmerCard() }
  }
}

@Composable
private fun ShimmerHeading(fraction: Float) =
  ShimmerLine(style = typography.titleMedium, modifier = Modifier.fillMaxWidth(fraction))

@Composable private fun ShimmerNode() = ShimmerBlock(Modifier.fillMaxSize(), shape = CircleShape)

@Composable private fun ShimmerCard() = ShimmerBlock(Modifier.fillMaxWidth().height(60.dp))

/** Keep in sync with [LoadingContent] */
@Composable
private fun LoadedContent(
  rule: Rule,
  conditionKeys: ImmutableList<Long>,
  actionKeys: ImmutableList<Long>,
  conditionEntities: ConditionEntities?,
  schedules: ImmutableList<Schedule>?,
  isScheduleRule: Boolean,
  isEnabled: Boolean,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: OnAction<EditRuleAction>,
  modifier: Modifier = Modifier,
) {
  val names: ImmutableMap<String, String>? = conditionEntities?.names
  // Sheets open on a key, so they stay on their condition or action as others come and go
  var openCondition by rememberSaveable { mutableStateOf<Long?>(null) }
  var isAddingCondition by rememberSaveable { mutableStateOf(false) }
  var openAction by rememberSaveable { mutableStateOf<Long?>(null) }
  var addingActionToSplit by rememberSaveable { mutableStateOf<Int?>(null) }
  var openSplit by rememberSaveable { mutableStateOf<Long?>(null) }

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
  ) {
    if (rule.hasUnknownValues) {
      item(key = "unknown-values") {
        WarningBanner(
          modifier = Modifier.animateItem().padding(bottom = Dimens.Huge),
          text = Strings.editRuleUnknownValues,
        )
      }
    }

    stageNode(stage = rule.stage, isEnabled = isEnabled, onAction = onAction)

    conditionsNode(
      conditions = rule.conditions,
      conditionKeys = conditionKeys,
      conditionsOp = rule.conditionsOp,
      names = names,
      isEnabled = isEnabled,
      onOpen = { key: Long -> openCondition = key }.takeIf { conditionEntities != null },
      onAdd = { isAddingCondition = true }.takeIf { conditionEntities != null },
      onAction = onAction,
    )

    actionsNode(
      actions = rule.actions,
      actionKeys = actionKeys,
      names = names,
      isEnabled = isEnabled,
      onOpen = { key: Long -> openAction = key }.takeIf { conditionEntities != null },
      onAdd = { split: Int -> addingActionToSplit = split }.takeIf { conditionEntities != null },
      onOpenSplit = { key: Long -> openSplit = key },
      onAction = onAction,
      schedules = schedules,
    )

    item(key = "bottom") {
      // Room to scroll the last card out from under the save button
      VerticalSpacer(FabClearance)
      BottomSpacing()
    }
  }

  if (conditionEntities != null) {
    openCondition?.let { key ->
      val condition = rule.conditions.getOrNull(conditionKeys.indexOf(key)) ?: return@let
      RuleConditionSheet(
        initial = condition,
        entities = conditionEntities,
        onDone = { onAction(SetCondition(key, it)) },
        onDelete = { onAction(RemoveCondition(key)) },
        canDelete = !isScheduleRule || condition.field != Date,
        onDismiss = { openCondition = null },
      )
    }

    if (isAddingCondition) {
      RuleConditionSheet(
        initial = newCondition(Payee, RuleConditions),
        entities = conditionEntities,
        onDone = { onAction(AddCondition(it)) },
        onDelete = null,
        onDismiss = { isAddingCondition = false },
      )
    }

    openAction?.let { key ->
      val action = rule.actions.getOrNull(actionKeys.indexOf(key)) ?: return@let
      RuleActionSheet(
        initial = action,
        entities = conditionEntities,
        schedules = schedules,
        onDone = { onAction(SetAction(key, it)) },
        onDelete = { onAction(RemoveAction(key)) },
        onDismiss = { openAction = null },
      )
    }

    addingActionToSplit?.let { split ->
      RuleActionSheet(
        initial = rule.actions.nextAction(split),
        entities = conditionEntities,
        schedules = schedules,
        onDone = { onAction(AddAction(it)) },
        onDelete = null,
        onDismiss = { addingActionToSplit = null },
      )
    }
  }

  openSplit?.let { key ->
    val amount = rule.actions.getOrNull(actionKeys.indexOf(key)) ?: return@let
    RuleSplitSheet(
      initial = amount,
      onDone = { onAction(SetAction(key, it)) },
      onRemove = { onAction(RemoveSplit(amount.splitIndex)) },
      onDismiss = { openSplit = null },
    )
  }
}

@PortraitPreview
@Composable
private fun PreviewEditRuleScaffold(
  @PreviewParameter(EditRuleStateProvider::class) params: ColorParams<EditRuleStateParams>,
) {
  PreviewWithColorParams(params) {
    EditRuleScaffold(
      state = state,
      mode = mode,
      conditionEntities = PREVIEW_ENTITIES,
      schedules = persistentListOf(),
      matches = MutableStateFlow(RuleMatchesState.Loaded(0, 0, persistentListOf())),
      onAction = {},
    )
  }
}

private data class EditRuleStateParams(val state: EditRuleState, val mode: Mode = Edit)

private class EditRuleStateProvider :
  ColorParamsProvider<EditRuleStateParams>(
    EditRuleStateParams(Success(PREVIEW_RULE, isWorking = false)),
    EditRuleStateParams(Success(PREVIEW_RULE, isWorking = false, hasChanges = true)),
    EditRuleStateParams(Success(PREVIEW_RULE, isWorking = true, hasChanges = true)),
    EditRuleStateParams(Success(PREVIEW_EMPTY_RULE, isWorking = false), mode = Create),
    EditRuleStateParams(Success(PREVIEW_UNKNOWN_RULE, isWorking = false)),
    EditRuleStateParams(Loading),
    EditRuleStateParams(EditRuleState.OtherFailure("Something broke")),
  )

private val PREVIEW_RULE =
  PreviewRule1.copy(
    conditions = persistentListOf(PreviewCondition2),
    actions =
      persistentListOf(
        RuleAction(
          field = Category,
          type = Id,
          value = JsonPrimitive("category-id"),
          op = RuleAction.Op.Set,
        ),
        RuleAction(field = Notes, type = null, value = JsonPrimitive("#online"), op = AppendNotes),
      ),
  )

private val PREVIEW_EMPTY_RULE =
  PreviewRule1.copy(conditions = persistentListOf(), actions = persistentListOf())

private val PREVIEW_UNKNOWN_RULE = PreviewRule1.copy(stage = Unknown)

private val PREVIEW_ENTITIES =
  ConditionEntityLists(
    accounts = persistentListOf(),
    categories = persistentListOf(),
    categoryGroups = persistentListOf(),
    payees = persistentListOf(),
    names = persistentMapOf("category-id" to "Shopping"),
  )

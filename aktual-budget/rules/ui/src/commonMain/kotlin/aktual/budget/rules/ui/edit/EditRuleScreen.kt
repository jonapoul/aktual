package aktual.budget.rules.ui.edit

import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.ConditionEntityLists
import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.ui.PreviewCondition2
import aktual.budget.rules.ui.PreviewRule1
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.edit.EditRuleState
import aktual.budget.rules.vm.edit.EditRuleState.Failure
import aktual.budget.rules.vm.edit.EditRuleState.Loading
import aktual.budget.rules.vm.edit.EditRuleState.Success
import aktual.budget.rules.vm.edit.EditRuleViewModel
import aktual.budget.rules.vm.edit.RuleMatchesState
import aktual.budget.rules.vm.hasUnknownValues
import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.material.DeleteForever
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.edit_rule_matches_applied
import aktual.core.l10n.edit_rule_matches_apply_failed
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualButton
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.Dimens
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.ProvideSurfaceColor
import aktual.core.ui.ShimmerBlock
import aktual.core.ui.ShimmerLine
import aktual.core.ui.WarningBanner
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.showErrorSnackbar
import aktual.core.ui.transparentTopAppBarColors
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
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
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

@Composable
internal fun EditRuleScreen(
  id: RuleId?,
  payee: PayeeId?,
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditRuleViewModel = metroViewModel(id, payee),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val conditionEntities by viewModel.conditionEntities.collectAsStateWithLifecycle()
  val matches by viewModel.matches.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        DeletedRule -> back()
        is AppliedActions ->
          snackbarHostState.showSnackbar(
            getPluralString(Res.plurals.edit_rule_matches_applied, event.count, event.count),
          )
        ApplyFailed ->
          snackbarHostState.showErrorSnackbar(getString(Res.string.edit_rule_matches_apply_failed))
      }
    }
  }

  CompositionLocalProvider(LocalNameFetcher provides viewModel.nameFetcher) {
    EditRuleScaffold(
      modifier = modifier,
      mode = if (id == null) Create else Edit,
      state = state,
      conditionEntities = conditionEntities,
      matches = matches,
      snackbarHostState = snackbarHostState,
      onAction = { action ->
        if (action == NavBack) back() else viewModel.handle(action)
      },
    )
  }
}

private fun EditRuleViewModel.handle(action: EditRuleAction) {
  when (action) {
    NavBack -> Unit
    Delete -> delete()
    Save -> save()
    is SetStage -> setStage(action.value)
    is SetConditionsOp -> setConditionsOp(action.value)
    is SetCondition -> setCondition(action.index, action.condition)
    AddCondition -> addCondition()
    is RemoveCondition -> removeCondition(action.index)
    is SetAction -> setAction(action.index, action.action)
    is AddAction -> addAction(action.splitIndex)
    is RemoveAction -> removeAction(action.index)
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
  matches: RuleMatchesState,
  onAction: OnAction<EditRuleAction>,
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val density = LocalDensity.current
  var headerHeight by remember { mutableStateOf(0.dp) }
  val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val peekHeight = if (state is Success) headerHeight + navBarHeight else 0.dp

  BottomSheetScaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    sheetPeekHeight = peekHeight,
    sheetSwipeEnabled = state is Success,
    sheetDragHandle = null,
    sheetContainerColor = colors.modalBackground,
    sheetContentColor = colors.pageText,
    sheetContent = {
      if (state is Success) {
        ProvideSurfaceColor(colors.modalBackground) {
          MatchesSheetContent(
            state = matches,
            isEnabled = !state.isWorking,
            onAction = onAction,
            headerModifier =
              Modifier.onSizeChanged { size ->
                headerHeight = with(density) { size.height.toDp() }
              },
          )
        }
      }
    },
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = {
          Text(if (mode == Create) Strings.editRuleToolbarCreate else Strings.editRuleToolbarEdit)
        },
        actions = {
          if (mode == Edit) {
            MoreMenu(isEnabled = state is Success && !state.isWorking, onAction = onAction)
          }
        },
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

        is Success -> {
          LoadedContent(
            modifier = contentModifier,
            listState = listState,
            contentPadding = contentPadding,
            bottomPadding = peekHeight,
            rule = state.rule,
            conditionKeys = state.conditionKeys,
            actionKeys = state.actionKeys,
            conditionEntities = conditionEntities,
            isEnabled = !state.isWorking,
            onAction = onAction,
          )

          AktualButton(
            modifier =
              Modifier.align(BottomEnd).padding(end = FabMargin, bottom = peekHeight + FabMargin),
            text = if (mode == Create) Strings.editRuleCreate else Strings.editRuleSave,
            icon = AktualIcons.Check,
            kind = Primary,
            shape = CardShape,
            contentPadding = FabPadding,
            isEnabled = state.canSave,
            isLoading = state.isWorking,
            onClick = { onAction(Save) },
          )
        }
      }
    }
  }
}

@Composable
private fun MoreMenu(isEnabled: Boolean, onAction: OnAction<EditRuleAction>) = Box {
  var showMenu by remember { mutableStateOf(false) }

  AktualIconButton(
    onClick = { showMenu = true },
    isEnabled = isEnabled,
    imageVector = AktualIcons.DotsHorizontalTriple,
    contentDescription = Strings.editRuleMenu,
  )

  AktualDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
    val string = Strings.editRuleActionDelete
    val red = colors.errorText
    AktualDropdownMenuItem(
      text = { Text(string, color = red) },
      onClick = {
        showMenu = false
        onAction(Delete)
      },
      leadingIcon = {
        Icon(imageVector = MaterialIcons.DeleteForever, contentDescription = string, tint = red)
      },
    )
  }
}

private val Failure.reason: String
  @Composable
  get() =
    when (this) {
      NotFound -> Strings.editRuleFailedNoMatch
      is SaveFailed -> Strings.editRuleFailedSaving(reason)
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
  isEnabled: Boolean,
  listState: LazyListState,
  contentPadding: PaddingValues,
  bottomPadding: Dp,
  onAction: OnAction<EditRuleAction>,
  modifier: Modifier = Modifier,
) {
  val names: ImmutableMap<String, String> = conditionEntities?.names ?: persistentMapOf()
  var openCondition by rememberSaveable { mutableStateOf<Int?>(null) }
  var openAction by rememberSaveable { mutableStateOf<Int?>(null) }
  var openSplit by rememberSaveable { mutableStateOf<Int?>(null) }

  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
  ) {
    if (rule.hasUnknownValues) {
      item(key = "unknown-values") {
        WarningBanner(
          modifier = Modifier.padding(bottom = Dimens.Huge),
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
      onOpen = { index: Int -> openCondition = index }.takeIf { conditionEntities != null },
      onAction = onAction,
    )

    actionsNode(
      actions = rule.actions,
      actionKeys = actionKeys,
      names = names,
      isEnabled = isEnabled,
      onOpen = { index: Int -> openAction = index }.takeIf { conditionEntities != null },
      onOpenSplit = { split: Int -> openSplit = split },
      onAction = onAction,
    )

    item(key = "bottom") {
      // Room to scroll the last card out from under the save button and the matches sheet
      VerticalSpacer(FabClearance + bottomPadding)
    }
  }

  val index = openCondition
  if (index != null && conditionEntities != null) {
    RuleConditionSheet(
      index = index,
      conditions = rule.conditions,
      entities = conditionEntities,
      onAction = onAction,
      onDismiss = { openCondition = null },
    )
  }

  val actionIndex = openAction
  if (actionIndex != null && conditionEntities != null) {
    RuleActionSheet(
      index = actionIndex,
      actions = rule.actions,
      entities = conditionEntities,
      onAction = onAction,
      onDismiss = { openAction = null },
    )
  }

  val splitIndex = openSplit
  if (splitIndex != null) {
    RuleSplitSheet(
      splitIndex = splitIndex,
      actions = rule.actions,
      onAction = onAction,
      onDismiss = { openSplit = null },
    )
  }
}

private val FabPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
private val FabClearance = 72.dp
private val FabMargin = 16.dp

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
      matches = RuleMatchesState.Loaded(0, 0, persistentListOf()),
      onAction = {},
    )
  }
}

private data class EditRuleStateParams(val state: EditRuleState, val mode: Mode = Edit)

private class EditRuleStateProvider :
  ColorParamsProvider<EditRuleStateParams>(
    EditRuleStateParams(Success(PREVIEW_RULE, isWorking = false)),
    EditRuleStateParams(Success(PREVIEW_RULE, isWorking = true)),
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

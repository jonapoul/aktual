package aktual.budget.rules.ui.list

import aktual.app.nav.BackNavigator
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.rules.vm.CheckboxesState
import aktual.budget.rules.vm.CheckboxesState.Active
import aktual.budget.rules.vm.CheckboxesState.Inactive
import aktual.budget.rules.vm.ListRulesState
import aktual.budget.rules.vm.ListRulesState.Empty
import aktual.budget.rules.vm.ListRulesState.Failure
import aktual.budget.rules.vm.ListRulesState.Loading
import aktual.budget.rules.vm.ListRulesState.Success
import aktual.budget.rules.vm.ListRulesViewModel
import aktual.budget.rules.vm.RuleListItem
import aktual.core.icons.AktualIcons
import aktual.core.icons.ChevronDown
import aktual.core.icons.ChevronUp
import aktual.core.icons.material.ClearAll
import aktual.core.icons.material.Delete
import aktual.core.icons.material.Deselect
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SelectAll
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BlurredTopBarSpacing
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.FailureScreen
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ListRulesScreen(
  @Suppress("unused") back: BackNavigator, // TODO
  modifier: Modifier = Modifier,
  viewModel: ListRulesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val checkboxes by viewModel.checkboxes.collectAsStateWithLifecycle()

  CompositionLocalProvider(LocalNameFetcher provides viewModel.nameFetcher) {
    ListRulesScaffold(
      modifier = modifier,
      state = state,
      checkboxes = checkboxes,
      onAction = { action ->
        when (action) {
          Reload -> viewModel.reload()
          is Delete -> viewModel.delete(action.id)
          is DeleteMultiple -> viewModel.delete(action.ids)
          is Open -> TODO()
          is Edit -> TODO()
          is OpenUrl -> TODO()
          is Check -> viewModel.check(action.id)
          is Uncheck -> viewModel.uncheck(action.id)
          UncheckAll -> viewModel.uncheckAll()
          DisableCheckboxes -> viewModel.hideCheckboxes()
          EnableCheckboxes -> viewModel.showCheckboxes()
        }
      },
    )
  }
}

@Composable
private fun ListRulesScaffold(
  state: ListRulesState,
  checkboxes: CheckboxesState,
  onAction: (ListRulesAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        title = { Text(Strings.rulesToolbar) },
        actions = { AppBarButtons(state, checkboxes, onAction) },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      Column(modifier = Modifier.blurredTopBarContent(blurState, innerPadding)) {
        BlurredTopBarSpacing(blurState, innerPadding)
        PullToRefreshBox(
          modifier = Modifier.padding(horizontal = 8.dp),
          contentAlignment = Alignment.Center,
          onRefresh = { onAction(Reload) },
          isRefreshing = state is Loading,
          content = { ListRulesContent(state, checkboxes, onAction, listState) },
        )
      }
    }
  }
}

@Composable
@Suppress("UnusedReceiverParameter")
private fun RowScope.AppBarButtons(
  state: ListRulesState,
  checkboxes: CheckboxesState,
  onAction: (ListRulesAction) -> Unit,
) {
  when (checkboxes) {
    is Active -> {
      BareIconButton(
        imageVector = MaterialIcons.Deselect,
        contentDescription = Strings.rulesToolbarCheckboxesDisable,
        enabled = state is Success,
        onClick = { onAction(DisableCheckboxes) },
      )
    }

    Inactive -> {
      BareIconButton(
        imageVector = MaterialIcons.SelectAll,
        contentDescription = Strings.rulesToolbarCheckboxesEnable,
        enabled = state is Success,
        onClick = { onAction(EnableCheckboxes) },
      )
    }
  }
}

@Composable
private fun ListRulesContent(
  state: ListRulesState,
  checkboxes: CheckboxesState,
  onAction: (ListRulesAction) -> Unit,
  listState: LazyListState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    when (state) {
      is Loading -> {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Medium)) {
          repeat(times = 10) { ShimmerRuleListItem(checkboxes) }
        }
      }

      is Failure -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          FailureScreen(
            modifier = Modifier.background(theme.tableBackground, CardShape),
            title = Strings.rulesFailurePrefix,
            reason = state.cause ?: Strings.rulesFailureDefaultMessage,
            onClickRetry = { onAction(Reload) },
            retryText = Strings.syncRetry,
          )
        }
      }

      is Empty -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(
            modifier = Modifier.background(theme.tableBackground, CardShape).padding(40.dp),
            text = Strings.rulesSuccessEmpty,
            color = theme.warningText,
          )
        }
      }

      is Success -> {
        ContentSuccess(
          rules = state.rules,
          checkboxes = checkboxes,
          listState = listState,
          onAction = onAction,
        )
      }
    }

    BottomStatusBarSpacing()
    BottomNavBarSpacing()
  }
}

@Composable
private fun CheckboxSelectionBar(
  selectedIds: ImmutableSet<RuleId>,
  onAction: (ListRulesAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier =
      modifier.fillMaxWidth().background(theme.modalBackground, CardShape).padding(Dimens.Large),
    horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val count = selectedIds.size
    Text(modifier = Modifier.weight(1f), text = Plurals.rulesCheckboxCounter(count, count))

    NormalIconButton(
      enabled = count > 0,
      imageVector = MaterialIcons.ClearAll,
      contentDescription = Strings.rulesCheckboxClear,
      onClick = { onAction(UncheckAll) },
    )

    NormalIconButton(
      enabled = count > 0,
      imageVector = MaterialIcons.Delete,
      contentDescription = Strings.rulesCheckboxDelete,
      colors = NormalRed,
      onClick = { onAction(DeleteMultiple(selectedIds)) },
    )
  }
}

@Composable
private fun ContentSuccess(
  rules: ImmutableList<RuleListItem>,
  checkboxes: CheckboxesState,
  onAction: (ListRulesAction) -> Unit,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberLazyListState(),
) {
  val stagedRules: ImmutableList<Pair<RuleStage, ImmutableList<RuleListItem>>> =
    remember(rules) {
      // always show all stages, even if no rules
      val grouped = rules.groupBy { it.stage }
      RuleStage.entries
        .map { stage -> stage to grouped[stage].orEmpty().toImmutableList() }
        .toImmutableList()
    }

  val expandedStages = remember {
    mutableStateMapOf(RuleStage.Pre to true, RuleStage.Default to true, RuleStage.Post to true)
  }

  Column(modifier) {
    Text(
      modifier = Modifier.padding(Dimens.Medium).fillMaxWidth(),
      textAlign = TextAlign.Start,
      text = headerText(onAction),
      style = AktualTypography.bodySmall,
    )

    LazyColumn(
      modifier = Modifier.scrollbar(listState).weight(1f),
      state = listState,
      verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
      stagedRules.forEach { (stage, r) ->
        val isExpanded = expandedStages.getValue(stage)
        stickyHeader { StageHeader(expandedStages, stage, isExpanded, r) }

        if (isExpanded) {
          items(r) { rule -> ListRulesItem(rule, checkboxes, onAction) }
        }
      }
    }

    AnimatedVisibility(
      visible = checkboxes is Active,
      enter = slideInVertically { it },
      exit = slideOutVertically { it },
      label = "CheckboxCounterBar",
    ) {
      if (checkboxes is Active) {
        CheckboxSelectionBar(
          modifier = Modifier.wrapContentHeight(),
          selectedIds = checkboxes.ids,
          onAction = onAction,
        )
      }
    }
  }
}

@Composable
private fun StageHeader(
  expandedStages: SnapshotStateMap<RuleStage, Boolean>,
  stage: RuleStage,
  isExpanded: Boolean,
  rules: ImmutableList<RuleListItem>,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .background(theme.pillBackgroundSelected, CardShape)
        .border(Dp.Hairline, theme.pillBorderDark, CardShape)
        .clickable { expandedStages[stage] = !isExpanded },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f).padding(Dimens.Large),
      text = Strings.rulesStagePrefix(stage.string()),
      style = AktualTypography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = theme.pillTextSelected,
    )

    Text(
      modifier = Modifier.padding(Dimens.Large),
      text = Strings.rulesStageCount(rules.size),
      style = AktualTypography.bodyLarge,
      textAlign = TextAlign.End,
      color = theme.pillText,
    )

    Icon(
      modifier = Modifier.minimumInteractiveComponentSize(),
      imageVector = if (isExpanded) AktualIcons.ChevronUp else AktualIcons.ChevronDown,
      contentDescription = "",
    )
  }
}

@PortraitPreview
@Composable
private fun PreviewListRulesScaffold(
  @PreviewParameter(ListRulesStateProvider::class) params: ThemedParams<ListRulesStateParams>
) =
  PreviewWithThemedParams(params) {
    ListRulesScaffold(state = state, checkboxes = checkboxes, onAction = {})
  }

private data class ListRulesStateParams(
  val state: ListRulesState,
  val checkboxes: CheckboxesState = Inactive,
)

private class ListRulesStateProvider :
  ThemedParameterProvider<ListRulesStateParams>(
    ListRulesStateParams(
      Success(persistentListOf(PreviewRuleListItem1, PreviewRuleListItem2)),
      checkboxes = Active(persistentSetOf(PreviewRuleListItem1.id)),
    ),
    ListRulesStateParams(Loading),
    ListRulesStateParams(Empty),
    ListRulesStateParams(Failure("Something broke")),
  )

package aktual.budget.rules.ui.list

import aktual.budget.model.RuleId
import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.ui.PreviewRule1
import aktual.budget.rules.ui.PreviewRule2
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.list.CheckboxesState
import aktual.budget.rules.vm.list.CheckboxesState.Active
import aktual.budget.rules.vm.list.CheckboxesState.Inactive
import aktual.budget.rules.vm.list.ListRulesState
import aktual.budget.rules.vm.list.ListRulesState.Empty
import aktual.budget.rules.vm.list.ListRulesState.Failure
import aktual.budget.rules.vm.list.ListRulesState.Loading
import aktual.budget.rules.vm.list.ListRulesState.Success
import aktual.budget.rules.vm.list.ListRulesViewModel
import aktual.core.icons.material.Add
import aktual.core.icons.material.ClearAll
import aktual.core.icons.material.Delete
import aktual.core.icons.material.Deselect
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.SelectAll
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.nav.EditRuleNavigator
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BlurredPullToRefreshBox
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Composable
fun ListRulesScreen(
  editRule: EditRuleNavigator,
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
          CreateNew -> editRule()
          is Delete -> viewModel.delete(action.id)
          is DeleteMultiple -> viewModel.delete(action.ids)
          is Edit -> editRule(action.id)
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
  onAction: ListRulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  // Drives the collapsing header below the toolbar: it fades + slides out as the list scrolls
  // down, and back in as it scrolls up (enter-always behaviour).
  val headerState = rememberCollapsingHeaderState()

  // Blur the bar as soon as content slips behind it. The header eats the first slice of scroll to
  // collapse itself, so the list won't report canScrollBackward yet — treat a collapsing header as
  // "scrolled" too, otherwise small scrolls leave a dead zone with no blur.
  val isScrolled by remember {
    derivedStateOf { listState.canScrollBackward || headerState.isCollapsing }
  }

  Scaffold(
    modifier = modifier.fillMaxSize().nestedScroll(headerState.nestedScrollConnection),
    topBar = {
      Column(modifier = Modifier.blurredTopBar(blurState, isScrolled = isScrolled)) {
        TopAppBar(
          colors = theme.transparentTopAppBarColors(),
          title = { Text(Strings.rulesToolbar) },
          actions = { AppBarButtons(state, checkboxes, onAction) },
        )
        CollapsingHeader(state = headerState, onAction = onAction)
      }
    },
  ) { innerPadding ->
    Box {
      PageBackground()
      BlurredPullToRefreshBox(
        modifier = Modifier.padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
        onRefresh = { onAction(Reload) },
        isRefreshing = state is Loading,
        blurState = blurState,
        innerPadding = innerPadding,
      ) { padding ->
        ListRulesContent(
          state = state,
          contentPadding = padding,
          checkboxes = checkboxes,
          onAction = onAction,
          listState = listState,
        )
      }
    }
  }
}

// Descriptive header that lives in the top app bar and collapses (fades + slides up), driven by
// [state] which the scaffold's nested-scroll connection updates as the list scrolls.
@Composable
private fun CollapsingHeader(
  state: CollapsingHeaderState,
  onAction: ListRulesActionHandler,
  modifier: Modifier = Modifier,
) {
  Text(
    modifier =
      modifier
        .fillMaxWidth()
        .clipToBounds()
        .layout { measurable, constraints ->
          val placeable = measurable.measure(constraints)
          state.height = placeable.height.toFloat()
          val collapsed = (placeable.height + state.offset).roundToInt().coerceAtLeast(0)
          layout(placeable.width, collapsed) { placeable.place(0, state.offset.roundToInt()) }
        }
        .graphicsLayer { alpha = state.alpha }
        .padding(horizontal = Dimens.Medium)
        .padding(bottom = Dimens.Medium),
    textAlign = TextAlign.Start,
    text = headerText(onAction),
    style = AktualTypography.bodySmall,
  )
}

@Composable
private fun AppBarButtons(
  state: ListRulesState,
  checkboxes: CheckboxesState,
  onAction: ListRulesActionHandler,
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
  contentPadding: PaddingValues,
  checkboxes: CheckboxesState,
  onAction: ListRulesActionHandler,
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
          repeat(times = 10) { ShimmerListRulesItem(checkboxes) }
        }
      }

      is Failure -> {
        FailureScreen(
          title = Strings.rulesFailurePrefix,
          reason = state.cause ?: Strings.rulesFailureDefaultMessage,
          background = theme.tableBackground,
          action =
            FailureAction(
              text = { Strings.syncRetry },
              icon = MaterialIcons.Refresh,
              onClick = { onAction(Reload) },
            ),
        )
      }

      is Empty -> {
        FailureScreen(
          title = Strings.rulesEmpty,
          reason = null,
          background = theme.tableBackground,
          action =
            FailureAction(
              text = { Strings.rulesEmptyCreate },
              icon = MaterialIcons.Add,
              onClick = { onAction(CreateNew) },
            ),
        )
      }

      is Success -> {
        ContentSuccess(
          rules = state.rules,
          checkboxes = checkboxes,
          listState = listState,
          contentPadding = contentPadding,
          onAction = onAction,
        )
      }
    }

    BottomSpacing()
  }
}

@Composable
private fun CheckboxSelectionBar(
  selectedIds: ImmutableSet<RuleId>,
  onAction: ListRulesActionHandler,
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
      isEnabled = count > 0,
      imageVector = MaterialIcons.ClearAll,
      contentDescription = Strings.rulesCheckboxClear,
      onClick = { onAction(UncheckAll) },
    )

    NormalIconButton(
      isEnabled = count > 0,
      imageVector = MaterialIcons.Delete,
      contentDescription = Strings.rulesCheckboxDelete,
      colors = NormalRed,
      onClick = { onAction(DeleteMultiple(selectedIds)) },
    )
  }
}

@Composable
private fun ContentSuccess(
  rules: ImmutableList<Rule>,
  checkboxes: CheckboxesState,
  contentPadding: PaddingValues,
  onAction: ListRulesActionHandler,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberLazyListState(),
) {
  Column(modifier) {
    // Pass the top inset as the LazyColumn's contentPadding (not Modifier.padding on the Column)
    // so items rest below the bar but scroll up *behind* it, which is what reveals the blur.
    LazyColumn(
      modifier = Modifier.scrollbar(listState).weight(1f),
      state = listState,
      contentPadding = contentPadding,
      verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
    ) {
      items(rules, key = { it.id.value }) { rule ->
        ListRulesItem(
          modifier = Modifier.animateItem(),
          rule = rule,
          checkboxes = checkboxes,
          onAction = onAction,
        )
      }

      item { BottomSpacing() }
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
      Success(persistentListOf(PreviewRule1, PreviewRule2)),
      checkboxes = Active(persistentSetOf(PreviewRule1.id)),
    ),
    ListRulesStateParams(Loading),
    ListRulesStateParams(Empty),
    ListRulesStateParams(Failure("Something broke")),
  )

package aktual.budget.rules.ui.list

import aktual.budget.model.RuleId
import aktual.budget.rules.ui.LocalNameFetcher
import aktual.budget.rules.ui.PreviewRule1
import aktual.budget.rules.ui.PreviewRule2
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.list.ListRulesState
import aktual.budget.rules.vm.list.ListRulesState.Empty
import aktual.budget.rules.vm.list.ListRulesState.Failure
import aktual.budget.rules.vm.list.ListRulesState.Loading
import aktual.budget.rules.vm.list.ListRulesState.Success
import aktual.budget.rules.vm.list.ListRulesViewModel
import aktual.core.icons.material.Add
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.icons.material.Search
import aktual.core.l10n.Strings
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.SearchRulesNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.topBarHazeOffset
import aktual.core.ui.transparentTopAppBarColors
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ListRulesScreen(
  editRule: EditRuleNavigator,
  toSearch: SearchRulesNavigator,
  modifier: Modifier = Modifier,
  viewModel: ListRulesViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  var pendingDelete by remember { mutableStateOf<RuleId?>(null) }
  val uriHandler = LocalUriHandler.current

  CompositionLocalProvider(LocalNameFetcher provides viewModel.nameFetcher) {
    ListRulesScaffold(
      modifier = modifier,
      state = state,
      onAction = { action ->
        when (action) {
          Retry -> viewModel.retry()
          CreateNew -> editRule()
          OpenSearch -> toSearch()
          is Delete -> pendingDelete = action.id
          is Edit -> editRule(action.id)
          is OpenUrl -> uriHandler.openUri(action.url)
        }
      },
    )
  }

  pendingDelete?.let { id ->
    DeleteRulesDialog(
      onConfirm = {
        viewModel.delete(id)
        pendingDelete = null
      },
      onDismiss = { pendingDelete = null },
    )
  }
}

@Composable
private fun ListRulesScaffold(
  state: ListRulesState,
  onAction: ListRulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()

  // Drives the collapsing header below the toolbar: it fades + slides out as the list scrolls
  // down, and back in as it scrolls up (enter-always behaviour).
  val headerState = rememberCollapsingHeaderState()

  Scaffold(
    modifier = modifier.fillMaxSize().nestedScroll(headerState.nestedScrollConnection),
    topBar = {
      // The header eats the first slice of scroll as it collapses, so fold its collapse distance
      // into the offset - otherwise small scrolls leave a dead zone with no haze.
      Column(
        modifier =
          Modifier.hazedTopBar(
            hazeState,
            scrollOffset = { -headerState.offset + listState.topBarHazeOffset() },
          ),
      ) {
        TopAppBar(
          colors = colors.transparentTopAppBarColors(),
          navigationIcon = { NavDrawerIconButton() },
          title = { Text(Strings.rulesToolbar) },
          actions = {
            if (state is Success) {
              BareIconButton(
                imageVector = MaterialIcons.Search,
                contentDescription = Strings.rulesToolbarSearch,
                onClick = { onAction(OpenSearch) },
              )
            }
          },
        )
        CollapsingHeader(state = headerState, onAction = onAction)
      }
    },
  ) { innerPadding ->
    Box {
      PageBackground()
      Box(
        modifier = Modifier.padding(horizontal = 8.dp).hazedTopBarContent(hazeState, innerPadding),
        contentAlignment = Center,
      ) {
        ListRulesContent(
          state = state,
          contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
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
        .padding(horizontal = 4.dp)
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
    textAlign = Start,
    text = headerText(onAction),
    style = typography.bodySmall,
  )
}

@Composable
private fun ListRulesContent(
  state: ListRulesState,
  contentPadding: PaddingValues,
  onAction: ListRulesActionHandler,
  listState: LazyListState,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = CenterHorizontally,
  ) {
    when (state) {
      is Loading -> {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.Medium)) {
          VerticalSpacer(contentPadding.calculateTopPadding())
          repeat(times = 10) { ShimmerListRulesItem() }
        }
      }

      is Failure -> {
        FailureScreen(
          title = Strings.rulesFailurePrefix,
          reason = state.cause ?: Strings.rulesFailureDefaultMessage,
          background = colors.tableBackground,
          action =
            FailureAction(
              text = { Strings.syncRetry },
              icon = MaterialIcons.Refresh,
              onClick = { onAction(Retry) },
            ),
        )
      }

      is Empty -> {
        FailureScreen(
          title = Strings.rulesEmpty,
          reason = null,
          background = colors.tableBackground,
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
private fun ContentSuccess(
  rules: ImmutableList<Rule>,
  contentPadding: PaddingValues,
  onAction: ListRulesActionHandler,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberLazyListState(),
) {
  // only one row may be swiped open at a time - opening another closes the previous one
  var openId by remember { mutableStateOf<RuleId?>(null) }

  // Pass the top inset as the LazyColumn's contentPadding (not Modifier.padding) so items rest
  // below
  // the bar but scroll up *behind* it, which is what reveals the haze.
  LazyColumn(
    modifier = modifier.scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
  ) {
    items(rules, key = { it.id.value }) { rule ->
      ListRulesItem(
        modifier = Modifier.animateItem(),
        rule = rule,
        isOpen = openId == rule.id,
        onOpenChange = { open ->
          openId =
            when {
              open -> rule.id
              openId == rule.id -> null
              else -> openId
            }
        },
        onAction = onAction,
      )
    }

    item { BottomSpacing() }
  }
}

@PortraitPreview
@Composable
private fun PreviewListRulesScaffold(
  @PreviewParameter(ListRulesStateProvider::class) params: ColoredParams<ListRulesStateParams>,
) =
  PreviewWithColoredParams(params) {
    ListRulesScaffold(state = state, onAction = {})
  }

private data class ListRulesStateParams(val state: ListRulesState)

private class ListRulesStateProvider :
  ColoredParameterProvider<ListRulesStateParams>(
    ListRulesStateParams(Success(persistentListOf(PreviewRule1, PreviewRule2))),
    ListRulesStateParams(Loading),
    ListRulesStateParams(Empty),
    ListRulesStateParams(Failure("Something broke")),
  )

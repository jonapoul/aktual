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
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Refresh
import aktual.core.icons.SearchAlternate
import aktual.core.l10n.Strings
import aktual.core.nav.EditRuleNavigator
import aktual.core.nav.SearchRulesNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ColorParams
import aktual.core.ui.Dimens
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.NoticeBanner
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
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

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { Text(Strings.rulesToolbar) },
        actions = {
          if (state is Success) {
            BareIconButton(
              imageVector = AktualIcons.SearchAlternate,
              contentDescription = Strings.rulesToolbarSearch,
              onClick = { onAction(OpenSearch) },
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()
      Box(
        modifier =
          Modifier.padding(horizontal = Dimens.ScreenPadding)
            .hazedTopBarContent(hazeState, innerPadding),
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
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing)) {
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
              icon = AktualIcons.Refresh,
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
              icon = AktualIcons.Add,
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
  // below the bar but scroll up *behind* it, which is what reveals the haze.
  LazyColumn(
    modifier = modifier.scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
  ) {
    item(key = "notice") {
      NoticeBanner(
        text = rulesHeaderText(),
        onClick = { onAction(OpenUrl(LEARN_MORE_URL)) },
      )
    }

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

@Composable internal expect fun rulesHeaderText(): String

private const val LEARN_MORE_URL = "https://actualbudget.org/docs/budgeting/rules/"

@PortraitPreview
@Composable
private fun PreviewListRulesScaffold(
    @PreviewParameter(ListRulesStateProvider::class) params: ColorParams<ListRulesStateParams>,
) =
  PreviewWithColorParams(params) {
    ListRulesScaffold(state = state, onAction = {})
  }

private data class ListRulesStateParams(val state: ListRulesState)

private class ListRulesStateProvider :
  ColorParamsProvider<ListRulesStateParams>(
    ListRulesStateParams(Success(persistentListOf(PreviewRule1, PreviewRule2))),
    ListRulesStateParams(Loading),
    ListRulesStateParams(Empty),
    ListRulesStateParams(Failure("Something broke")),
  )

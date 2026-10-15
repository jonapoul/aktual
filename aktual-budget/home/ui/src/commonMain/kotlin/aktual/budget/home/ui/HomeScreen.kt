package aktual.budget.home.ui

import aktual.budget.home.vm.AccountsCardState
import aktual.budget.home.vm.HomeCardType
import aktual.budget.home.vm.HomeState
import aktual.budget.home.vm.HomeViewModel
import aktual.core.icons.material.Check
import aktual.core.icons.material.Edit
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.nav.BankSyncNavigator
import aktual.core.nav.BankSyncSettingsNavigator
import aktual.core.nav.EditScheduleNavigator
import aktual.core.nav.LinkBankAccountNavigator
import aktual.core.nav.ListSchedulesNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.DesktopPreview
import aktual.core.ui.Dimens
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.TabletPreview
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun HomeScreen(
  transactions: TransactionsNavigator,
  bankSync: BankSyncNavigator,
  bankSyncSettings: BankSyncSettingsNavigator,
  linkBankAccount: LinkBankAccountNavigator,
  schedules: ListSchedulesNavigator,
  editSchedule: EditScheduleNavigator,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  HomeScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        is OpenAccount -> transactions(action.id)
        OpenBankSync -> bankSync()
        is OpenBankSyncSettings -> bankSyncSettings(action.id)
        is LinkBankAccount -> linkBankAccount(action.id)
        ReviewUncategorised -> transactions.uncategorised()
        is OpenSchedule -> editSchedule(action.id)
        OpenSchedules -> schedules()
        Retry -> viewModel.retry()
        is SwapCards -> viewModel.swap(action.card, action.other)
      }
    },
    modifier = modifier,
  )
}

// Measured against the screen's own width rather than the window's, since the nav rail takes some
// of it. A brand new budget has no accounts to list, so the onboarding card gets the whole width
@Composable
private fun HomeScaffold(
  state: HomeState,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
  initialEditing: Boolean = false,
) {
  var isEditing by rememberSaveable { mutableStateOf(initialEditing) }
  val onToggleEditing = { isEditing = !isEditing }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    if (maxWidth < SidePanelMinWidth || state.isEmpty) {
      HomeContent(state = state, isEditing = isEditing, onToggleEditing = onToggleEditing) {
        if (state.isEmpty) {
          OnboardingCard(onAction = onAction)
        } else {
          ReorderableCards(
            cards = state.cards,
            columns = 1,
            isEditing = isEditing,
            onAction = onAction,
          ) { card ->
            HomeCardFor(card = card, state = state, onAction = onAction)
          }
        }
      }
    } else {
      Row(modifier = Modifier.fillMaxSize()) {
        AccountsPanel(
          modifier = Modifier.width(AccountsPanelWidth).fillMaxHeight(),
          state = state.accounts,
          onAction = onAction,
        )

        VerticalDivider(color = colors.tableBorder)

        HomeContent(
          state = state,
          isEditing = isEditing,
          onToggleEditing = onToggleEditing,
          modifier = Modifier.weight(1f),
        ) {
          CardGrid(state = state, isEditing = isEditing, onAction = onAction)
        }
      }
    }
  }
}

@Composable
private fun HomeCardFor(card: HomeCardType, state: HomeState, onAction: HomeActionHandler) =
  when (card) {
    ThisMonth -> ThisMonthCard(state = state.thisMonth, onAction = onAction)
    Attention -> AttentionCard(state = state.attention, onAction = onAction)
    Upcoming -> UpcomingCard(state = state.upcoming, onAction = onAction)
    Accounts -> AccountsCard(state = state.accounts, onAction = onAction)
  }

@Composable
private fun HomeContent(
  state: HomeState,
  isEditing: Boolean,
  onToggleEditing: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { HomeTitle(budgetName = state.budgetName) },
        actions = {
          if (!state.isEmpty) {
            Crossfade(targetState = isEditing) { editing ->
              BareIconButton(
                imageVector = if (editing) MaterialIcons.Check else MaterialIcons.Edit,
                contentDescription = if (editing) Strings.homeReorderDone else Strings.homeReorder,
                onClick = onToggleEditing,
              )
            }
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      Column(
        modifier =
          Modifier.fillMaxSize()
            .hazedTopBarContent(hazeState, innerPadding)
            .verticalScrollWithBar(scrollState)
            .padding(hazedTopBarContentPadding(hazeState, innerPadding))
            .padding(Dimens.VeryLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.VeryLarge),
      ) {
        content()
        BottomSpacing()
      }
    }
  }
}

// Scrolls separately from the cards, so the account list is always to hand
@Composable
private fun AccountsPanel(
  state: AccountsCardState,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .background(colors.tableBackground)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(Top))
        .verticalScrollWithBar(rememberScrollState()),
  ) {
    AccountsCard(state = state, onAction = onAction, isBoxed = false, showAll = true)
    BottomSpacing()
  }
}

// Two columns once there's room for both, capped so the cards don't stretch across a wide monitor.
// Accounts has its own panel here
@Composable
private fun CardGrid(
  state: HomeState,
  isEditing: Boolean,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  val cards = remember(state.cards) { state.cards.filter { it != Accounts }.toImmutableList() }
  BoxWithConstraints(modifier = modifier.widthIn(max = CardGridMaxWidth)) {
    ReorderableCards(
      cards = cards,
      columns = if (maxWidth < TwoColumnMinWidth) 1 else 2,
      isEditing = isEditing,
      onAction = onAction,
    ) { card ->
      HomeCardFor(card = card, state = state, onAction = onAction)
    }
  }
}

private val AccountsPanelWidth = 360.dp
private val TwoColumnMinWidth = 640.dp
private val SidePanelMinWidth = AccountsPanelWidth + 400.dp
private val CardGridMaxWidth = 1200.dp

@Composable
private fun HomeTitle(budgetName: String?, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text(text = Strings.homeTitle, maxLines = 1, overflow = Ellipsis)
    if (budgetName != null) {
      Text(
        text = budgetName,
        style = MaterialTheme.typography.bodySmall,
        color = colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
    }
  }
}

private const val PREVIEW_BUDGET_NAME = "Household budget"

private val PREVIEW_HOME =
  HomeState(
    budgetName = PREVIEW_BUDGET_NAME,
    thisMonth = PREVIEW_THIS_MONTH,
    attention = PREVIEW_ATTENTION,
    upcoming = PREVIEW_UPCOMING,
    accounts = AccountsCardState.Loaded(PREVIEW_ACCOUNTS),
  )

private class HomeStateProvider :
  ColoredParameterProvider<HomeState>(
    PREVIEW_HOME,
    HomeState(budgetName = PREVIEW_BUDGET_NAME),
    HomeState(
      budgetName = null,
      thisMonth = PREVIEW_THIS_MONTH,
      attention = Empty,
      upcoming = Empty,
      accounts = Empty,
    ),
    HomeState(
      budgetName = PREVIEW_BUDGET_NAME,
      thisMonth = Failed,
      attention = Failed,
      upcoming = Failed,
      accounts = Failed,
    ),
  )

private class WideHomeStateProvider : ColoredParameterProvider<HomeState>(PREVIEW_HOME)

private class PrivateHomeStateProvider : ColoredParameterProvider<HomeState>(PREVIEW_HOME)

@PortraitPreview
@Composable
private fun PreviewHomeScaffold(
  @PreviewParameter(HomeStateProvider::class) params: ColoredParams<HomeState>,
) =
  PreviewWithColoredParams(params) {
    HomeScaffold(state = this, onAction = {})
  }

@TabletPreview
@Composable
private fun PreviewTabletHomeScaffold(
  @PreviewParameter(HomeStateProvider::class) params: ColoredParams<HomeState>,
) =
  PreviewWithColoredParams(params) {
    HomeScaffold(state = this, onAction = {})
  }

@DesktopPreview
@Composable
private fun PreviewDesktopHomeScaffold(
  @PreviewParameter(WideHomeStateProvider::class) params: ColoredParams<HomeState>,
) =
  PreviewWithColoredParams(params) {
    HomeScaffold(state = this, onAction = {})
  }

@PortraitPreview
@Composable
private fun PreviewEditingHomeScaffold(
  @PreviewParameter(WideHomeStateProvider::class) params: ColoredParams<HomeState>,
) =
  PreviewWithColoredParams(params) {
    HomeScaffold(state = this, onAction = {}, initialEditing = true)
  }

@DesktopPreview
@Composable
private fun PreviewEditingDesktopHomeScaffold(
  @PreviewParameter(WideHomeStateProvider::class) params: ColoredParams<HomeState>,
) =
  PreviewWithColoredParams(params) {
    HomeScaffold(state = this, onAction = {}, initialEditing = true)
  }

@PortraitPreview
@Composable
private fun PreviewPrivateHomeScaffold(
  @PreviewParameter(PrivateHomeStateProvider::class) params: ColoredParams<HomeState>,
) =
  PreviewWithColoredParams(params, isPrivacyEnabled = true) {
    HomeScaffold(state = this, onAction = {})
  }

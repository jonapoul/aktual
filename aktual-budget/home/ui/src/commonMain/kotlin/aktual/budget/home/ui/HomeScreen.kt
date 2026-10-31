package aktual.budget.home.ui

import aktual.budget.accounts.components.AddAccountSheet
import aktual.budget.accounts.components.addAccount
import aktual.budget.accounts.vm.AddAccountViewModel
import aktual.budget.home.vm.AccountsCardState
import aktual.budget.home.vm.AttentionCardState
import aktual.budget.home.vm.HomeCardType
import aktual.budget.home.vm.HomeState
import aktual.budget.home.vm.HomeViewModel
import aktual.budget.home.vm.ReportsCardState
import aktual.budget.home.vm.UpcomingCardState
import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.Pencil1
import aktual.core.l10n.Strings
import aktual.core.nav.BankSyncNavigator
import aktual.core.nav.BankSyncSettingsNavigator
import aktual.core.nav.CreateAccountNavigator
import aktual.core.nav.EditScheduleNavigator
import aktual.core.nav.EditTransactionNavigator
import aktual.core.nav.LinkBankAccountNavigator
import aktual.core.nav.ListSchedulesNavigator
import aktual.core.nav.ReportNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.DesktopPreview
import aktual.core.ui.Dimens
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun HomeScreen(
  transactions: TransactionsNavigator,
  editTransaction: EditTransactionNavigator,
  bankSync: BankSyncNavigator,
  bankSyncSettings: BankSyncSettingsNavigator,
  createAccount: CreateAccountNavigator,
  linkBankAccount: LinkBankAccountNavigator,
  schedules: ListSchedulesNavigator,
  editSchedule: EditScheduleNavigator,
  report: ReportNavigator,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = metroViewModel(),
  addAccountViewModel: AddAccountViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  var showAddAccount by rememberSaveable { mutableStateOf(false) }

  HomeScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        is OpenAccount -> transactions(action.id)
        is NewTransaction -> editTransaction.create(account = action.account)
        OpenBankSync -> bankSync()
        AddAccount ->
          addAccount(
            canBankSync = addAccountViewModel.canBankSync,
            showSheet = { showAddAccount = true },
            createLocal = { createAccount() },
          )
        is OpenBankSyncSettings -> bankSyncSettings(action.id)
        is LinkBankAccount -> linkBankAccount(action.id)
        ReviewUncategorised -> transactions.uncategorised()
        is OpenSchedule -> editSchedule(action.id)
        OpenSchedules -> schedules()
        Retry -> viewModel.retry()
        is OpenReport -> report(action.id)
        is PinReport -> viewModel.pinReport(action.id)
        is UnpinReport -> viewModel.unpinReport(action.id)
        is ReplaceReport -> viewModel.replaceReport(action.old, action.new)
        is SwapCards -> viewModel.swap(action.card, action.other)
        is ReorderCards -> viewModel.reorder(action.cards)
      }
    },
    modifier = modifier,
  )

  if (showAddAccount) {
    AddAccountSheet(
      onCreateLocal = { createAccount() },
      onBankSync = { bankSync() },
      onDismiss = { showAddAccount = false },
    )
  }
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
  val scrollState = rememberScrollState()
  val scrollArea = remember(scrollState) { AutoScrollArea(scrollState) }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    if (maxWidth < SidePanelMinWidth || state.isEmpty) {
      HomeContent(
        state = state,
        isEditing = isEditing,
        scrollArea = scrollArea,
        onToggleEditing = onToggleEditing,
      ) {
        if (state.isEmpty) {
          OnboardingCard(onAction = onAction)
        } else {
          ReorderableCards(
            cards = state.cards,
            columns = 1,
            isEditing = isEditing,
            scrollArea = scrollArea,
            onAction = onAction,
          ) { card ->
            HomeCardFor(card = card, state = state, isEditing = isEditing, onAction = onAction)
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
          scrollArea = scrollArea,
          onToggleEditing = onToggleEditing,
          modifier = Modifier.weight(1f),
        ) {
          CardGrid(
            state = state,
            isEditing = isEditing,
            scrollArea = scrollArea,
            onAction = onAction,
          )
        }
      }
    }
  }
}

@Composable
private fun HomeCardFor(
  card: HomeCardType,
  state: HomeState,
  isEditing: Boolean,
  onAction: HomeActionHandler,
) =
  when (card) {
    ThisMonth -> ThisMonthCard(state = state.thisMonth, onAction = onAction)

    Attention ->
      if (isEditing && state.attention.isHidden) {
        HiddenCard(message = Strings.homeAttentionHidden)
      } else {
        AttentionCard(state = state.attention, onAction = onAction)
      }

    Upcoming ->
      if (isEditing && state.upcoming.isHidden) {
        HiddenCard(message = Strings.homeUpcomingHidden)
      } else {
        UpcomingCard(state = state.upcoming, onAction = onAction)
      }

    Accounts -> AccountsCard(state = state.accounts, onAction = onAction)

    Reports ->
      if (isEditing && state.reports.isHidden) {
        HiddenCard(message = Strings.homeReportsHidden)
      } else {
        ReportsCard(state = state.reports, choices = state.reportChoices, onAction = onAction)
      }
  }

@Composable
private fun HomeContent(
  state: HomeState,
  isEditing: Boolean,
  scrollArea: AutoScrollArea,
  onToggleEditing: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = scrollArea.scrollState

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
                imageVector = if (editing) AktualIcons.Check else AktualIcons.Pencil1,
                contentDescription = if (editing) Strings.homeReorderDone else Strings.homeReorder,
                onClick = onToggleEditing,
              )
            }
          }
        },
      )
    },
  ) { innerPadding ->
    val density = LocalDensity.current
    val topInset = with(density) { innerPadding.calculateTopPadding().toPx() }
    val bottomInset = with(density) { innerPadding.calculateBottomPadding().toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      Column(
        modifier =
          Modifier.fillMaxSize()
            .hazedTopBarContent(hazeState, innerPadding)
            .onGloballyPositioned { scrollArea.place(it, topInset, bottomInset) }
            .verticalScrollWithBar(scrollState)
            .padding(hazedTopBarContentPadding(hazeState, innerPadding))
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ListItemSpacing),
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
  scrollArea: AutoScrollArea,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  val cards = remember(state.cards) { state.cards.filter { it != Accounts }.toImmutableList() }
  BoxWithConstraints(modifier = modifier.widthIn(max = CardGridMaxWidth)) {
    ReorderableCards(
      cards = cards,
      columns = if (maxWidth < TwoColumnMinWidth) 1 else 2,
      isEditing = isEditing,
      scrollArea = scrollArea,
      onAction = onAction,
    ) { card ->
      HomeCardFor(card = card, state = state, isEditing = isEditing, onAction = onAction)
    }
  }
}

// These cards draw nothing when empty
private val AttentionCardState.isHidden: Boolean
  get() = this == Empty

private val UpcomingCardState.isHidden: Boolean
  get() = this == Empty

private val ReportsCardState.isHidden: Boolean
  get() = this == Empty

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
    reports = PREVIEW_REPORTS,
    reportChoices = PREVIEW_REPORT_CHOICES,
  )

private class HomeStateProvider :
  ColorParamsProvider<HomeState>(
    PREVIEW_HOME,
    HomeState(budgetName = PREVIEW_BUDGET_NAME),
    HomeState(
      budgetName = null,
      thisMonth = PREVIEW_THIS_MONTH,
      attention = Empty,
      upcoming = Empty,
      accounts = Empty,
      reports = Empty,
    ),
    HomeState(
      budgetName = PREVIEW_BUDGET_NAME,
      thisMonth = Failed,
      attention = Failed,
      upcoming = Failed,
      accounts = Failed,
      reports = Failed,
    ),
  )

private class WideHomeStateProvider : ColorParamsProvider<HomeState>(PREVIEW_HOME)

private class PrivateHomeStateProvider : ColorParamsProvider<HomeState>(PREVIEW_HOME)

@PortraitPreview
@Composable
private fun PreviewHomeScaffold(
  @PreviewParameter(HomeStateProvider::class) params: ColorParams<HomeState>,
) =
  PreviewWithColorParams(params) {
    HomeScaffold(state = this, onAction = {})
  }

@TabletPreview
@Composable
private fun PreviewTabletHomeScaffold(
  @PreviewParameter(HomeStateProvider::class) params: ColorParams<HomeState>,
) =
  PreviewWithColorParams(params) {
    HomeScaffold(state = this, onAction = {})
  }

@DesktopPreview
@Composable
private fun PreviewDesktopHomeScaffold(
  @PreviewParameter(WideHomeStateProvider::class) params: ColorParams<HomeState>,
) =
  PreviewWithColorParams(params) {
    HomeScaffold(state = this, onAction = {})
  }

@PortraitPreview
@Composable
private fun PreviewEditingHomeScaffold(
  @PreviewParameter(WideHomeStateProvider::class) params: ColorParams<HomeState>,
) =
  PreviewWithColorParams(params) {
    HomeScaffold(state = this, onAction = {}, initialEditing = true)
  }

@DesktopPreview
@Composable
private fun PreviewEditingDesktopHomeScaffold(
  @PreviewParameter(WideHomeStateProvider::class) params: ColorParams<HomeState>,
) =
  PreviewWithColorParams(params) {
    HomeScaffold(state = this, onAction = {}, initialEditing = true)
  }

@PortraitPreview
@Composable
private fun PreviewPrivateHomeScaffold(
  @PreviewParameter(PrivateHomeStateProvider::class) params: ColorParams<HomeState>,
) =
  PreviewWithColorParams(params, isPrivacyEnabled = true) {
    HomeScaffold(state = this, onAction = {})
  }

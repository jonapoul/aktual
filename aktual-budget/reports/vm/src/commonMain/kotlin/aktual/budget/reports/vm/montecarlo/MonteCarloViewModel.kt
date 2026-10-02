package aktual.budget.reports.vm.montecarlo

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.model.AccountId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.DashboardSync
import aktual.budget.reports.vm.McConfig
import aktual.budget.reports.vm.MonteCarloReportMeta
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.runMonteCarlo
import aktual.budget.reports.vm.toConfig
import aktual.budget.reports.vm.toMeta
import aktual.budget.reports.vm.withLiveBalances
import aktual.di.BudgetScope
import alakazam.kotlin.CoroutineContexts
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.logcat

// packages/desktop-client/src/components/reports/reports/monte-carlo/MonteCarlo.tsx
@Stable
@AssistedInject
@OptIn(ExperimentalCoroutinesApi::class)
class MonteCarloViewModel
internal constructor(
  @Assisted private val id: WidgetId,
  dashboardDao: DashboardDao,
  accountDao: AccountDao,
  reportsDao: ReportsDao,
  decoder: DashboardItemDecoder,
  private val sync: DashboardSync,
  contexts: CoroutineContexts,
) : ViewModel() {
  private val saved: StateFlow<SavedMeta> =
    dashboardDao
      .observeById(id)
      .map { widget ->
        val meta = widget?.let(decoder::decode)?.meta
        if (meta is MonteCarloReportMeta) SavedMeta.Found(meta) else SavedMeta.NotFound
      }
      .stateIn(viewModelScope, Eagerly, SavedMeta.Loading)

  // The plan being edited, taken from the saved meta once and only changed by the user from then on
  private val mutableConfig = MutableStateFlow<McConfig?>(null)
  private val mutableShowTodaysMoney = MutableStateFlow(true)
  private val mutableResultsView = MutableStateFlow(MonteCarloResultsView.Chart)
  private val mutableGraphView = MutableStateFlow(MonteCarloGraphView.All)
  private val mutableCashflowPercentile = MutableStateFlow(RunPercentile.Median)
  private val mutableSelectedRun = MutableStateFlow<SelectedRun?>(null)

  private val resolvedConfig: StateFlow<McConfig?> =
    mutableConfig
      .flatMapLatest { config ->
        if (config == null) {
          flowOf(null)
        } else {
          liveBalances(reportsDao, config).map(config::withLiveBalances)
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  private val simulation: StateFlow<Simulation?> =
    combine(resolvedConfig.filterNotNull(), mutableShowTodaysMoney, ::Pair)
      .distinctUntilChanged()
      .mapLatest { (config, deflate) ->
        withContext(contexts.default) { simulate(config, deflate) }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  private val runDetail: StateFlow<MonteCarloRunDetail?> =
    combine(
        simulation,
        mutableResultsView,
        mutableCashflowPercentile,
        selectedRunIndex(),
        ::detailRunIndex,
      )
      .distinctUntilChanged()
      .mapLatest { target ->
        target?.let { (sim, index) -> withContext(contexts.default) { capture(sim, index) } }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  private val accounts: StateFlow<ImmutableList<MonteCarloAccount>> = flow {
    emit(accountDao.getAllWithStatus())
  }
    .map { rows ->
      rows
        .map { row ->
          MonteCarloAccount(
            id = row.id,
            name = row.name.orEmpty(),
            isClosed = row.closed == true,
            isOffBudget = row.offbudget == true,
          )
        }
        .toImmutableList()
    }
    .stateIn(viewModelScope, Eagerly, persistentListOf())

  val state: StateFlow<MonteCarloState> =
    viewModelScope.launchMolecule(Immediate) {
      val saved by saved.collectAsState()
      val config by resolvedConfig.collectAsState()
      val edited by mutableConfig.collectAsState()
      val simulation by simulation.collectAsState()
      val runDetail by runDetail.collectAsState()
      val accounts by accounts.collectAsState()
      val showTodaysMoney by mutableShowTodaysMoney.collectAsState()
      val resultsView by mutableResultsView.collectAsState()
      val graphView by mutableGraphView.collectAsState()
      val cashflowPercentile by mutableCashflowPercentile.collectAsState()
      val selectedRun by mutableSelectedRun.collectAsState()

      when (val s = saved) {
        SavedMeta.Loading -> {
          MonteCarloState.Loading
        }

        SavedMeta.NotFound -> {
          MonteCarloState.NotFound
        }

        is SavedMeta.Found -> {
          val current = config
          if (current == null) {
            MonteCarloState.Loading
          } else {
            MonteCarloState.Loaded(
              title = s.meta.name,
              config = current,
              accounts = accounts,
              hasChanges = edited != s.meta.toConfig(),
              showTodaysMoney = showTodaysMoney,
              resultsView = resultsView,
              graphView = graphView,
              cashflowPercentile = cashflowPercentile,
              results = simulation?.results,
              selectedRun = selectedRun?.takeIf { it.config == edited }?.index,
              runDetail = runDetail,
            )
          }
        }
      }
    }

  init {
    viewModelScope.launch {
      val found = saved.first { it != SavedMeta.Loading } as? SavedMeta.Found ?: return@launch
      mutableConfig.update { found.meta.toConfig() }
    }
  }

  fun setConfig(config: McConfig) = mutableConfig.update { config }

  fun setShowTodaysMoney(show: Boolean) = mutableShowTodaysMoney.update { show }

  fun setResultsView(view: MonteCarloResultsView) = mutableResultsView.update { view }

  fun setGraphView(view: MonteCarloGraphView) = mutableGraphView.update { view }

  fun setCashflowPercentile(percentile: RunPercentile) = mutableCashflowPercentile.update {
    percentile
  }

  // A selected run refers to a specific simulation, so it expires when the plan changes
  fun selectRun(index: Int?) = mutableSelectedRun.update {
    index?.let { run -> SelectedRun(mutableConfig.value, run) }
  }

  fun save() {
    val meta = (saved.value as? SavedMeta.Found)?.meta ?: return
    val config = resolvedConfig.value ?: return
    logcat.d { "Saving Monte Carlo config for $id" }
    // Saving the resolved plan keeps linked pots' stored balances fresh as a fallback
    mutableConfig.update { config }
    viewModelScope.launch { sync.setMonteCarloConfig(id, config.toMeta(meta)) }
  }

  // Throws away unsaved edits
  fun reset() {
    val meta = (saved.value as? SavedMeta.Found)?.meta ?: return
    mutableConfig.update { meta.toConfig() }
  }

  private fun selectedRunIndex(): Flow<Int?> =
    combine(mutableSelectedRun, mutableConfig) { selected, config ->
      selected?.takeIf { it.config == config }?.index
    }

  private fun detailRunIndex(
    simulation: Simulation?,
    view: MonteCarloResultsView,
    percentile: RunPercentile,
    selectedRun: Int?,
  ): Pair<Simulation, Int>? {
    if (simulation == null) return null
    val index =
      when (view) {
        MonteCarloResultsView.Cashflow -> simulation.runAt(percentile)
        MonteCarloResultsView.Runs -> selectedRun
        MonteCarloResultsView.Chart -> null
      }
    return index?.let { simulation to it }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(id: WidgetId): MonteCarloViewModel
  }
}

private sealed interface SavedMeta {
  data object Loading : SavedMeta

  data object NotFound : SavedMeta

  data class Found(val meta: MonteCarloReportMeta) : SavedMeta
}

private data class SelectedRun(val config: McConfig?, val index: Int)

private fun liveBalances(dao: ReportsDao, config: McConfig): Flow<Map<AccountId, Long>> {
  val linked = config.pots.mapNotNull { it.accountId }.toSet()
  if (linked.isEmpty()) return flowOf(emptyMap())
  return dao.observeMonteCarloAccountBalances(linked).map { rows ->
    rows.associate { it.account to it.total }
  }
}

internal fun capture(simulation: Simulation, index: Int): MonteCarloRunDetail {
  val rows =
    runMonteCarlo(simulation.config, deflate = simulation.deflate, captureRunDetail = index)
      .runDetail
      .orEmpty()
  return MonteCarloRunDetail(
    index = index,
    rows = rows.toImmutableList(),
    cashflow = buildCashflowChart(rows, simulation.config, CHART_PALETTE_SIZE),
  )
}

// The size of the theme's qualitative chart palette
private const val CHART_PALETTE_SIZE = 9

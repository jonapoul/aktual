package aktual.di

import aktual.budget.db.SqlDriverFactory
import aktual.budget.model.DbMetadata
import aktual.budget.model.cloudFileId
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import alakazam.kotlin.StateHolder
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Inject
@SingleIn(AppScope::class)
internal class RunLevelHolder(private val driverFactory: SqlDriverFactory) :
  StateHolder<List<AktualGraph>>(initialState = emptyList()),
  AutoCloseable,
  RunLevelState,
  RunLevelController {
  override fun viewModelGraph(): Flow<ViewModelGraph> = map { it.last() }.distinctUntilChanged()

  @Suppress("UNCHECKED_CAST")
  override fun <G : AktualGraph> get(type: KClass<G>): G? =
    value.firstOrNull(type::isInstance) as? G

  private inline fun <reified G : AktualGraph> observeNullable(): Flow<G?> =
    map { levels -> levels.filterIsInstance<G>().singleOrNull() }.distinctUntilChanged()

  override fun all(): Flow<List<AktualGraph>> = this

  override fun app(): Flow<AppGraph> =
    map { levels -> levels.filterIsInstance<AppGraph>().single() }.distinctUntilChanged()

  override fun serverChosen(): Flow<ServerChosenGraph?> = observeNullable<ServerChosenGraph>()

  override fun loggedIn(): Flow<LoggedInGraph?> = observeNullable<LoggedInGraph>()

  override fun budget(): Flow<BudgetGraph?> = observeNullable<BudgetGraph>()

  override fun init(graphs: List<AktualGraph>) {
    assertAllDistinct(graphs)
    graphs.forEach { it.initialize() }
    update { levels ->
      closeAll(levels)
      graphs.sorted()
    }
  }

  override fun onServerChosen(url: ServerUrl): ServerChosenGraph {
    var serverChosenGraph: ServerChosenGraph? = null
    update { levels ->
      val appGraph = levels[AppGraph::class]
      serverChosenGraph = appGraph.serverChosenGraphFactory.create(url)
      serverChosenGraph.initialize()
      (levels + serverChosenGraph).also(::assertAllDistinct).sorted()
    }
    return requireNotNull(serverChosenGraph)
  }

  override fun onLoggedIn(token: Token): LoggedInGraph {
    var loggedInGraph: LoggedInGraph? = null
    update { levels ->
      val serverChosenGraph = levels[ServerChosenGraph::class]
      loggedInGraph = serverChosenGraph.loggedInGraphFactory.create(token)
      loggedInGraph.initialize()
      (levels + loggedInGraph).also(::assertAllDistinct).sorted()
    }
    return requireNotNull(loggedInGraph)
  }

  override fun onBudget(metadata: DbMetadata): BudgetGraph {
    var budgetGraph: BudgetGraph? = null
    update { levels ->
      val loggedInGraph = levels[LoggedInGraph::class]
      val driver = driverFactory.create(metadata.cloudFileId)
      budgetGraph =
        loggedInGraph.budgetGraphFactory.create(id = metadata.cloudFileId, metadata, driver)
      budgetGraph.initialize()
      (levels + budgetGraph).also(::assertAllDistinct).sorted()
    }
    return requireNotNull(budgetGraph)
  }

  override fun onBudgetClosed() = update { levels -> levels.popTo<LoggedInGraph>().sorted() }

  override fun onLoggedOut() = update { levels -> levels.popTo<ServerChosenGraph>().sorted() }

  override fun onServerCleared() = update { levels -> levels.popTo<AppGraph>().sorted() }

  override fun close() {
    update { levels ->
      closeAll(levels.asReversed())
      emptyList()
    }
  }

  private inline fun <reified G : AktualGraph> List<AktualGraph>.popTo(): List<AktualGraph> {
    val keepIndex = indexOfFirst { it is G }
    require(keepIndex >= 0) { "Level ${G::class.qualifiedName} not found in $this" }
    closeAll(drop(keepIndex + 1).asReversed())
    return take(keepIndex + 1)
  }

  private fun closeAll(levels: List<AktualGraph>) = levels.forEach { it.close() }

  private fun assertAllDistinct(levels: List<AktualGraph>) {
    val distinct = levels.distinctBy { it::class }
    require(levels.size == distinct.size) {
      "Found multiple run levels of the same type: levels=$levels distinct=$distinct"
    }
  }

  @Suppress("UNCHECKED_CAST")
  private operator fun <G : AktualGraph> List<AktualGraph>.get(type: KClass<G>): G =
    single { type.isInstance(it) } as G
}

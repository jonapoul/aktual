package aktual.di

import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope

sealed interface AktualGraph : ViewModelGraph, Initializable, Closeable, Comparable<AktualGraph> {
  val closeables: Set<Closeable>
  val initializables: Set<Initializable>
  val coroutineScope: CoroutineScope

  @Multibinds(allowEmpty = true) val accessors: Map<KClass<out Any>, Accessor>

  @Suppress("UNCHECKED_CAST")
  operator fun <T : Any> get(klass: KClass<out T>): T =
    requireNotNull(accessors[klass]?.invoke() as? T) {
      "No accessor registered for $klass in $this"
    }

  override fun compareTo(other: AktualGraph): Int = runLevelIndex.compareTo(other.runLevelIndex)

  override fun initialize() = initializables.forEach(Initializable::initialize)

  override fun close() = closeables.forEach(Closeable::close)
}

private val AktualGraph.runLevelIndex: Int
  get() =
    when (this) {
      is AppGraph -> 0
      is ServerChosenGraph -> 1
      is LoggedInGraph -> 2
      is BudgetGraph -> 3
    }

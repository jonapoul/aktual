package aktual.di

import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import logcat.logcat

sealed interface AktualGraph : ViewModelGraph, Initializable, Closeable, Comparable<AktualGraph> {
  @Multibinds(allowEmpty = true) val closeables: Set<Closeable>
  @Multibinds(allowEmpty = true) val initializables: Set<Initializable>

  override fun compareTo(other: AktualGraph): Int = runLevelIndex.compareTo(other.runLevelIndex)

  override fun initialize() {
    initializables.forEach { i ->
      val result = runCatching { i.initialize() }
      if (result.isFailure) {
        logcat.e(result.exceptionOrNull()) { "Failed initialising $i" }
      }
    }
  }

  override fun close() {
    closeables.forEach { c ->
      val result = runCatching { c.close() }
      if (result.isFailure) {
        logcat.e(result.exceptionOrNull()) { "Failed closing $c" }
      }
    }
  }
}

private val AktualGraph.runLevelIndex: Int
  get() =
    when (this) {
      is AppGraph -> 0
      is ServerChosenGraph -> 1
      is LoggedInGraph -> 2
      is BudgetGraph -> 3
    }

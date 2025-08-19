package actual.core.di

interface AppGraph : BudgetGraph.Factory, ViewModelGraph.Factory {
  val viewModelGraphProvider: ViewModelGraphProvider

  fun interface Holder {
    operator fun invoke(): AppGraph
  }
}

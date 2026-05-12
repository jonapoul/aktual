package aktual.di

interface AppGraph : AktualGraph {
  val serverChosenGraphFactory: ServerChosenGraph.Factory
}

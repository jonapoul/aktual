package aktual.di

fun interface RunLevelInitialiser {
  suspend operator fun invoke(appGraph: AppGraph)
}

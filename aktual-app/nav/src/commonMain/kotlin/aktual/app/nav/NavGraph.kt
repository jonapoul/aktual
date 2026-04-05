package aktual.app.nav

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension

@GraphExtension(NavScope::class)
interface NavGraph {
  val navEntryContributors: Set<NavEntryContributor>

  @GraphExtension.Factory
  @ContributesTo(AppScope::class)
  fun interface Factory {
    fun create(): NavGraph
  }
}

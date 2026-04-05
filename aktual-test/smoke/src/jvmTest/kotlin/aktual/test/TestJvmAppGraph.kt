package aktual.test

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(scope = AppScope::class, excludes = [TestBuildConfigContainer::class])
interface TestJvmAppGraph : TestAppGraph

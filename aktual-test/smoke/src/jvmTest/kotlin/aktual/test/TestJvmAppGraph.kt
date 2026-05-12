package aktual.test

import aktual.di.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(scope = AppScope::class, excludes = [TestBuildConfigContainer::class])
interface TestJvmAppGraph : TestAppGraph

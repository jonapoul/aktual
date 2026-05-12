package aktual.test

import aktual.di.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metrox.android.MetroAppComponentProviders

@DependencyGraph(scope = AppScope::class, excludes = [TestBuildConfigContainer::class])
interface TestAndroidAppGraph : TestAppGraph, MetroAppComponentProviders

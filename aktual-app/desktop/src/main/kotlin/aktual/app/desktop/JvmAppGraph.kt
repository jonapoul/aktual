package aktual.app.desktop

import aktual.core.model.BuildConfig
import aktual.di.AppGraph
import aktual.di.AppScope
import aktual.di.RunLevelInitialiser
import aktual.di.RunLevelState
import aktual.logging.LogStorage
import aktual.prefs.AppIconController
import aktual.prefs.WindowStatePreferences
import dev.zacsweers.metro.DependencyGraph
import kotlin.time.Clock

@DependencyGraph(AppScope::class)
interface JvmAppGraph : AppGraph {
  val buildConfig: BuildConfig
  val windowPreferences: WindowStatePreferences
  val appIconController: AppIconController
  val clock: Clock
  val logStorage: LogStorage
  val initialiser: RunLevelInitialiser
  val runLevelState: RunLevelState
}

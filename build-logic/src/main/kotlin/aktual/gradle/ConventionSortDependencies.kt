package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import com.squareup.sort.SortDependenciesExtension
import com.squareup.sort.SortDependenciesPlugin
import org.gradle.api.Project

class ConventionSortDependencies : ProjectPlugin {
  override fun Project.applyTo() {
    if (path == ":aktual-app:desktop") {
      // Dependency Sorter fails parsing when we have the Nucleus config in the build file
      return
    }

    pluginManager.apply(SortDependenciesPlugin::class)

    extensions.configure(SortDependenciesExtension::class) {
      insertBlankLines.set(false)
    }
  }
}

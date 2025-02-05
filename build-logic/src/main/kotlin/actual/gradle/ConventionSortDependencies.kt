package actual.gradle

import com.squareup.sort.SortDependenciesExtension
import com.squareup.sort.SortDependenciesPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ConventionSortDependencies : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(SortDependenciesPlugin::class.java)
    }

    extensions.configure<SortDependenciesExtension> {
      insertBlankLines.set(false)
    }
  }
}

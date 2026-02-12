package aktual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

class ConventionIdea : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply(IdeaPlugin::class)

      extensions.configure<IdeaModel> {
        module {
          isDownloadJavadoc = true
          isDownloadSources = true
        }
      }
    }
}

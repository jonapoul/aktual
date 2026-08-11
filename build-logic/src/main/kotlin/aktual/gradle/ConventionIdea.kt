package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

class ConventionIdea : ProjectPlugin {
  override fun Project.applyTo() {
    pluginManager.apply(IdeaPlugin::class)

    extensions.configure(IdeaModel::class) {
      module { m ->
        m.isDownloadJavadoc = true
        m.isDownloadSources = true
      }
    }
  }
}

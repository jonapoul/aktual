package actual.diagrams

import guru.nidi.graphviz.attribute.Color
import org.gradle.api.Project

enum class ModuleType(val label: String, val color: Color) {
  AndroidApp(string = "App", fill = "#FF5555"), // red
  AndroidViewModel(string = "ViewModel", fill = "#F5A6A6"), // pink
  AndroidHilt(string = "Hilt", fill = "#FCB103"), // orange
  AndroidCompose(string = "Compose", fill = "#FFFF55"), // yellow
  AndroidLibrary(string = "Android", fill = "#55FF55"), // green
  AndroidResources(string = "Resources", fill = "#00FFFF"), // cyan
  Multiplatform(string = "Multiplatform", fill = "#9D8DF1"), // indigo
  Jvm(string = "JVM", fill = "#8000FF"), // violet
  ;

  constructor(string: String, fill: String) : this(string, Color.rgb(fill).fill())

  companion object {
    fun find(project: Project): ModuleType = with(project.plugins) {
      when {
        hasPlugin("com.android.application") -> AndroidApp
        hasPlugin("actual.module.viewmodel") -> AndroidViewModel
        hasPlugin("actual.module.hilt") -> AndroidHilt
        hasPlugin("actual.module.compose") -> AndroidCompose
        hasPlugin("actual.module.android") -> AndroidLibrary
        hasPlugin("actual.module.resources") -> AndroidResources
        hasPlugin("actual.module.multiplatform") -> Multiplatform
        hasPlugin("actual.module.jvm") -> Jvm
        else -> error("Unknown module type for ${project.path}")
      }
    }
  }
}

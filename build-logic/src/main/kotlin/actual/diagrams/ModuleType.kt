package actual.diagrams

import guru.nidi.graphviz.attribute.Color
import org.gradle.api.Project

interface ModuleType {
  val string: String
  val color: String

  fun interface Finder {
    fun find(project: Project): ModuleType
  }
}

fun ModuleType.Finder.color(project: Project): Color = Color.rgb(find(project).color).fill()

enum class ActualModuleType(override val string: String, override val color: String) : ModuleType {
  AndroidApp(string = "App", color = "#FF5555"), // red
  AndroidViewModel(string = "ViewModel", color = "#F5A6A6"), // pink
  AndroidHilt(string = "Hilt", color = "#FCB103"), // orange
  AndroidCompose(string = "Compose", color = "#FFFF55"), // yellow
  AndroidLibrary(string = "Android", color = "#55FF55"), // green
  AndroidResources(string = "Resources", color = "#00FFFF"), // cyan
  Navigation(string = "Navigation", color = "#5555FF"), // blue
  Multiplatform(string = "Multiplatform", color = "#9D8DF1"), // indigo
  Kotlin(string = "JVM", color = "#BB00FF"), // violet
  ;

  object Finder : ModuleType.Finder {
    override fun find(project: Project): ModuleType = when {
      project.plugins.hasPlugin("com.android.application") -> AndroidApp
      project.plugins.hasPlugin("actual.module.hilt") -> AndroidHilt
      project.plugins.hasPlugin("actual.module.viewmodel") -> AndroidViewModel
      project.plugins.hasPlugin("actual.module.compose") -> AndroidCompose
      project.plugins.hasPlugin("actual.module.android") -> AndroidLibrary
      project.plugins.hasPlugin("actual.module.resources") -> AndroidResources
      project.plugins.hasPlugin("actual.module.navigation") -> Navigation
      project.plugins.hasPlugin("actual.module.multiplatform") -> Multiplatform
      project.plugins.hasPlugin("actual.module.kotlin") -> Kotlin
      else -> error("Unknown module type for ${project.path}")
    }
  }
}

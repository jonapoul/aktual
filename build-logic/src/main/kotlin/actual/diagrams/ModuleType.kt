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

@Suppress("MagicNumber")
fun ModuleType.Finder.color(project: Project): Color {
  val found = find(project).color
  if (found.length != 7 || found.first() != '#') {
    error("Invalid colour found for ${project.path}: '$found'")
  }
  return Color.rgb(found).fill()
}

enum class ActualModuleType(override val string: String, override val color: String) : ModuleType {
  AndroidApp(string = "App", color = "#FF5555"), // red
  AndroidViewModel(string = "ViewModel", color = "#F5A6A6"), // pink
  AndroidHilt(string = "Hilt", color = "#FCB103"), // orange
  AndroidCompose(string = "Compose", color = "#FFFF55"), // yellow
  AndroidLibrary(string = "Android", color = "#55FF55"), // green
  AndroidResources(string = "Resources", color = "#00FFFF"), // cyan
  Navigation(string = "Navigation", color = "#5555FF"), // blue
  Multiplatform(string = "Multiplatform", color = "#9D8DF1"), // indigo
  Jvm(string = "JVM", color = "#8000FF"), // violet
  ;

  companion object {
    val Finder = ModuleType.Finder { project ->
      with(project.plugins) {
        when {
          hasPlugin("com.android.application") -> AndroidApp
          hasPlugin("actual.module.viewmodel") -> AndroidViewModel
          hasPlugin("actual.module.hilt") -> AndroidHilt
          hasPlugin("actual.module.compose") -> AndroidCompose
          hasPlugin("actual.module.android") -> AndroidLibrary
          hasPlugin("actual.module.resources") -> AndroidResources
          hasPlugin("actual.module.navigation") -> Navigation
          hasPlugin("actual.module.multiplatform") -> Multiplatform
          hasPlugin("actual.module.jvm") -> Jvm
          else -> error("Unknown module type for ${project.path}")
        }
      }
    }
  }
}

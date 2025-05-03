package actual.diagrams

import guru.nidi.graphviz.attribute.Color
import org.gradle.api.Project

interface ModuleType {
  val string: String
  val color: Color

  fun interface Finder {
    fun find(project: Project): ModuleType
    fun color(project: Project): Color = find(project).color.also { println("COLOR = $it") }
  }
}

enum class ActualModuleType(override val string: String, override val color: Color) : ModuleType {
  AndroidApp(string = "App", fill = "#FF5555"), // red
  AndroidViewModel(string = "ViewModel", fill = "#F5A6A6"), // pink
  AndroidHilt(string = "Hilt", fill = "#FCB103"), // orange
  AndroidCompose(string = "Compose", fill = "#FFFF55"), // yellow
  AndroidLibrary(string = "Android", fill = "#55FF55"), // green
  AndroidResources(string = "Resources", fill = "#00FFFF"), // cyan
  Navigation(string = "Navigation", fill = "#5555FF"), // blue
  Multiplatform(string = "Multiplatform", fill = "#9D8DF1"), // indigo
  Jvm(string = "JVM", fill = "#8000FF"), // violet
  ;

  constructor(string: String, fill: String) : this(string, Color.rgb(fill).fill())

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

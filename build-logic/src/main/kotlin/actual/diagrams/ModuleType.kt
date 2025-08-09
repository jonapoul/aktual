package actual.diagrams

import actual.diagrams.ModuleType.Android
import actual.diagrams.ModuleType.App
import actual.diagrams.ModuleType.Compose
import actual.diagrams.ModuleType.DI
import actual.diagrams.ModuleType.Jvm
import actual.diagrams.ModuleType.Multiplatform
import actual.diagrams.ModuleType.Other
import actual.diagrams.ModuleType.ViewModel
import org.gradle.api.Project

enum class ModuleType {
  App,
  ViewModel,
  DI,
  Compose,
  Android,
  Multiplatform,
  Jvm,
  Other,
}

internal fun moduleType(project: Project): ModuleType = with(project) {
  when {
    hasPlugin("actual.module.viewmodel") -> ViewModel
    hasPlugin("actual.module.di") -> DI
    hasPlugin("actual.module.compose") -> Compose
    hasPlugin("actual.module.android") -> Android
    hasPlugin("actual.module.multiplatform") -> Multiplatform
    path.contains("app:") -> App
    hasPlugin("actual.module.jvm") -> Jvm
    else -> Other
  }
}

private fun Project.hasPlugin(id: String) = pluginManager.hasPlugin(id)

internal val ModuleType.label: String
  get() = when (this) {
    App -> "App"
    ViewModel -> "ViewModel"
    DI -> "DI"
    Compose -> "Compose"
    Android -> "Android"
    Multiplatform -> "Multiplatform"
    Jvm -> "JVM"
    Other -> "Other"
  }

internal val ModuleType.color: String
  get() = when (this) {
    App -> "#FF5555" // red
    ViewModel -> "#F5A6A6" // pink
    DI -> "#FCB103" // orange
    Compose -> "#FFFF55" // yellow
    Android -> "#55FF55" // green
    Multiplatform -> "#9D8DF1" // indigo
    Jvm -> "#8000FF" // violet
    Other -> "#808080" // grey
  }

package actual.diagrams

import actual.diagrams.ModuleType.AndroidApp
import actual.diagrams.ModuleType.AndroidCompose
import actual.diagrams.ModuleType.AndroidHilt
import actual.diagrams.ModuleType.AndroidLibrary
import actual.diagrams.ModuleType.AndroidResources
import actual.diagrams.ModuleType.AndroidViewModel
import actual.diagrams.ModuleType.Jvm
import actual.diagrams.ModuleType.Multiplatform
import actual.diagrams.ModuleType.Other
import guru.nidi.graphviz.attribute.Color
import org.gradle.api.Project

enum class ModuleType {
  AndroidApp,
  AndroidViewModel,
  AndroidHilt,
  AndroidCompose,
  AndroidLibrary,
  AndroidResources,
  Multiplatform,
  Jvm,
  Other,
}

internal fun ModuleType(project: Project) = with(project.pluginManager) {
  when {
    hasPlugin("com.android.application") -> AndroidApp
    hasPlugin("actual.module.viewmodel") -> AndroidViewModel
    hasPlugin("actual.module.hilt") -> AndroidHilt
    hasPlugin("actual.module.compose") -> AndroidCompose
    hasPlugin("actual.module.android") -> AndroidLibrary
    hasPlugin("actual.module.resources") -> AndroidResources
    hasPlugin("actual.module.multiplatform") -> Multiplatform
    hasPlugin("actual.module.jvm") -> Jvm
    else -> Other
  }
}

internal val ModuleType.label: String
  get() = when (this) {
    AndroidApp -> "App"
    AndroidViewModel -> "ViewModel"
    AndroidHilt -> "Hilt"
    AndroidCompose -> "Compose"
    AndroidLibrary -> "Android"
    AndroidResources -> "Resources"
    Multiplatform -> "Multiplatform"
    Jvm -> "JVM"
    Other -> "Other"
  }

internal val ModuleType.color: Color
  get() = when (this) {
    AndroidApp -> "#FF5555" // red
    AndroidViewModel -> "#F5A6A6" // pink
    AndroidHilt -> "#FCB103" // orange
    AndroidCompose -> "#FFFF55" // yellow
    AndroidLibrary -> "#55FF55" // green
    AndroidResources -> "#00FFFF" // cyan
    Multiplatform -> "#9D8DF1" // indigo
    Jvm -> "#8000FF" // violet
    Other -> "#808080" // grey
  }.let(Color::rgb).fill()

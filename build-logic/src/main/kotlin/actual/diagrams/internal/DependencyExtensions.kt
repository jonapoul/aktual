package actual.diagrams.internal

import guru.nidi.graphviz.attribute.Color
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency

internal fun ResolvedDependency.depColour(): Color {
  val hash = moduleGroup.hashCode()
  val hexString = "%06X".format(hash).take(n = 6)
  return Color.rgb("#$hexString").fill()
}

internal fun ResolvedDependency.toNiceString(target: Project): String {
  val group = if (moduleGroup.startsWith("${target.rootProject.name}.")) {
    moduleGroup.removePrefix("${target.rootProject.name}.")
  } else {
    moduleGroup
  }
  return "$group:$moduleName"
}

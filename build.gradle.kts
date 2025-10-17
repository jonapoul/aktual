import atlas.graphviz.ArrowType
import atlas.graphviz.FileFormat
import atlas.graphviz.LayoutEngine
import atlas.graphviz.LinkStyle
import atlas.graphviz.NodeStyle
import atlas.graphviz.RankDir
import atlas.graphviz.Shape
import blueprint.core.rootLocalPropertiesOrNull

plugins {
  alias(libs.plugins.agp.app) apply false
  alias(libs.plugins.agp.lib) apply false
  alias(libs.plugins.androidCacheFix) apply false
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.burst) apply false
  alias(libs.plugins.catalog) apply false
  alias(libs.plugins.compose) apply false
  alias(libs.plugins.composeHotReload) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.powerAssert) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.licensee) apply false
  alias(libs.plugins.manifestLock) apply false
  alias(libs.plugins.metro) apply false
  alias(libs.plugins.redacted) apply false
  alias(libs.plugins.sqldelight) apply false

  alias(libs.plugins.atlas)
  alias(libs.plugins.doctor)
  alias(libs.plugins.kover)
  alias(libs.plugins.spotless)

  alias(libs.plugins.convention.idea)
  alias(libs.plugins.convention.kover)
  alias(libs.plugins.convention.spotless)
}

// Place all local properties in the project-level gradle properties map
rootLocalPropertiesOrNull()?.forEach { (key, value) ->
  ext[key.toString()] = value.toString()
}

doctor {
  javaHome {
    ensureJavaHomeMatches = false
    ensureJavaHomeIsSet = true
    failOnError = true
  }
}

atlas {
  generateOnSync = false
  groupModules = false

  pathTransforms { replace(pattern = ":modules:", replacement = ":") }

  moduleTypes {
    registerByPluginId(name = "ViewModel", pluginId = "actual.module.viewmodel", color = "#914141") // pink
    registerByPluginId(name = "DI", pluginId = "actual.module.di", color = "#a17103") // orange
    registerByPluginId(name = "UI", pluginId = "actual.module.compose", color = "#6b6b01") // yellow
    registerByPluginId(name = "Android", pluginId = "actual.module.android", color = "#017001") // green
    registerByPluginId(name = "Multiplatform", pluginId = "actual.module.multiplatform", color = "#160185") // indigo
    registerByPathContains(name = "App", pathContains = "app:", color = "#7a0101") // red
    registerByPluginId(name = "JVM", pluginId = "actual.module.jvm", color = "#2f015c") // violet
    other(color = "#808080") // grey
  }

  linkTypes {
    api(LinkStyle.Bold)
    implementation(LinkStyle.Dashed)
  }

  graphviz {
    fileFormat = FileFormat.Png
    layoutEngine = LayoutEngine.Dot

    node {
      shape = Shape.Box
      style = NodeStyle.Filled
      fontColor = "white"
      lineColor = "transparent"
    }

    edge {
      arrowHead = ArrowType.Normal
      linkColor = "white"
    }

    graph {
      bgColor = "#1c1c1c"
      rankDir = RankDir.TopToBottom
      rankSep = 1.5
    }
  }
}

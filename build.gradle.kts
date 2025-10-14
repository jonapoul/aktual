import atlas.d2.ArrowType
import atlas.d2.Direction
import atlas.d2.ElkAlgorithm
import atlas.d2.FileFormat
import atlas.d2.LinkStyle
import atlas.d2.Theme
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

  alias(libs.plugins.convention.idea)
  alias(libs.plugins.convention.kover)
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
    registerByPluginId(name = "ViewModel", pluginId = "actual.module.viewmodel", color = "#F5A6A6") // pink
    registerByPluginId(name = "DI", pluginId = "actual.module.di", color = "#FCB103") // orange
    registerByPluginId(name = "UI", pluginId = "actual.module.compose", color = "#FFFF55") // yellow
    registerByPluginId(name = "Android", pluginId = "actual.module.android", color = "#55FF55") // green
    registerByPluginId(name = "Multiplatform", pluginId = "actual.module.multiplatform", color = "#9D8DF1") // indigo
    registerByPathContains(name = "App", pathContains = "app:", color = "#FF5555") // red
    registerByPluginId(name = "JVM", pluginId = "actual.module.jvm", color = "#8000FF") // violet
    other(color = "#808080") // grey
  }

  linkTypes {
    api(LinkStyle.Bold)
    implementation(LinkStyle.Dashed)
  }

  d2 {
    animateLinks = true
    center = true
    direction = Direction.Down
    fileFormat = FileFormat.Svg
    pad = 5
    sketch = true
    theme = Theme.ShirleyTemple
    themeDark = Theme.DarkMauve

    layoutEngine.elk {
      algorithm = ElkAlgorithm.Layered
      edgeNodeBetweenLayers = 20
      nodeNodeBetweenLayers = 20
      nodeSelfLoop = 10
    }

    rootStyle {
      fill = "transparent"
    }

    globalProps {
      arrowType = ArrowType.Arrow
      fillArrowHeads = true
    }
  }
}

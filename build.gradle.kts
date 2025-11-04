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
  alias(libs.plugins.shadow) apply false
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
  checkOutputs = false
  generateOnSync = false
  groupModules = false

  pathTransforms { remove(":aktual-") }

  moduleTypes {
    registerByPluginId(name = "ViewModel", pluginId = "aktual.module.viewmodel", color = "#914141") // pink
    registerByPluginId(name = "DI", pluginId = "aktual.module.di", color = "#a17103") // orange
    registerByPluginId(name = "UI", pluginId = "aktual.module.compose", color = "#6b6b01") // yellow
    registerByPluginId(name = "Android", pluginId = "aktual.module.android", color = "#017001") // green
    registerByPluginId(name = "Multiplatform", pluginId = "aktual.module.multiplatform", color = "#160185") // indigo
    registerByPathContains(name = "App", pathContains = ":aktual-app:", color = "#7a0101") // red
    registerByPluginId(name = "JVM", pluginId = "aktual.module.jvm", color = "#2f015c") // violet
    other(color = "#808080") // grey
  }

  linkTypes {
    api(LinkStyle.Bold)
    implementation(LinkStyle.Dashed)
  }

  d2 {
    animateLinks = false
    center = true
    direction = Direction.Down
    fileFormat = FileFormat.Svg
    pad = 20
    theme = Theme.DarkFlagshipTerrastruct

    rootStyle {
      fill = "#1C1C1C"
    }

    globalProps {
      arrowType = ArrowType.Arrow
      fillArrowHeads = true
      fontSize = 20
    }

    layoutEngine {
      elk {
        algorithm = ElkAlgorithm.Layered
        edgeNodeBetweenLayers = 15
        nodeNodeBetweenLayers = 25
        padding = "top=10,left=10,bottom=10,right=10"
      }
    }
  }
}

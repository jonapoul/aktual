import atlas.d2.ArrowType.Arrow
import atlas.d2.Direction.Down
import atlas.d2.ElkAlgorithm.Layered
import atlas.d2.FileFormat.Svg
import atlas.d2.LinkStyle.Bold
import atlas.d2.LinkStyle.Dashed
import atlas.d2.Theme.DarkFlagshipTerrastruct
import atlas.d2.tasks.SvgToPng.Converter.ImageMagick6
import blueprint.core.rootLocalPropertiesOrNull

plugins {
  alias(libs.plugins.agp.app) apply false
  alias(libs.plugins.agp.lib) apply false
  alias(libs.plugins.androidCacheFix) apply false
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.burst) apply false
  alias(libs.plugins.catalog) apply false
  alias(libs.plugins.compose) apply false
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
  pathTransforms { remove(":aktual-") }

  projectTypes {
    hasPluginId(name = "ViewModel", pluginId = "aktual.module.viewmodel", color = "#914141") // pink
    hasPluginId(name = "DI", pluginId = "aktual.module.di", color = "#a17103") // orange
    hasPluginId(name = "UI", pluginId = "aktual.module.compose", color = "#6b6b01") // yellow
    hasPluginId(name = "Android", pluginId = "aktual.module.android", color = "#017001") // green
    hasPluginId(name = "Multiplatform", pluginId = "aktual.module.multiplatform", color = "#160185") // indigo
    pathContains(name = "App", pathContains = ":aktual-app:", color = "#7a0101") // red
    hasPluginId(name = "JVM", pluginId = "aktual.module.jvm", color = "#2f015c") // violet
    other(color = "#808080") // grey
  }

  linkTypes {
    api(Bold)
    implementation(Dashed)
  }

  d2 {
    animateLinks = false
    center = true
    direction = Down
    fileFormat = Svg
    pad = 20
    theme = DarkFlagshipTerrastruct
    convertSvgToPng(ImageMagick6)

    rootStyle {
      fill = "#1C1C1C"
    }

    globalProps {
      arrowType = Arrow
      fillArrowHeads = true
      fontSize = 20
    }

    layoutEngine {
      elk {
        algorithm = Layered
        edgeNodeBetweenLayers = 15
        nodeNodeBetweenLayers = 25
        padding = "top=10,left=10,bottom=10,right=10"
      }
    }
  }
}

import aktual.gradle.detektTasks
import atlas.graphviz.ArrowType.None
import atlas.graphviz.ArrowType.Normal
import atlas.graphviz.FileFormat.Png
import atlas.graphviz.LayoutEngine.Dot
import atlas.graphviz.LinkStyle.Dashed
import atlas.graphviz.LinkStyle.Solid
import atlas.graphviz.NodeStyle.Filled
import atlas.graphviz.RankDir.TopToBottom
import atlas.graphviz.Shape.Box
import dev.detekt.gradle.report.ReportMergeTask

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

val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
  output = layout.buildDirectory.file("reports/detekt/merge.sarif.json")
}

tasks.check.configure {
  dependsOn(detektReportMergeSarif)
}

allprojects {
  detektReportMergeSarif.configure {
    input.from(detektTasks.map { it.reports.sarif.outputLocation })
  }
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
    "commonMainApi"(Solid, "white", displayName = "api")
    "commonMainImplementation"(Dashed, "aqua", displayName = "implementation")
  }

  graphviz {
    fileFormat = Png
    layoutEngine = Dot

    graph {
      bgColor = "#00000A"
      rankDir = TopToBottom
      rankSep = 1.5
    }

    node {
      style = Filled
      shape = Box
      fontColor = "white"
      fillColor = "black"
    }

    edge {
      arrowHead = Normal
      arrowTail = None
      linkColor = "white"
    }
  }
}

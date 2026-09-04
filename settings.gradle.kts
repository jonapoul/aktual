import atlas.core.LinkStyle.Dashed
import atlas.core.LinkStyle.Solid
import atlas.graphviz.ArrowType.None
import atlas.graphviz.ArrowType.Normal
import atlas.graphviz.FileFormat.Png
import atlas.graphviz.LayoutEngine.Dot
import atlas.graphviz.NodeStyle.Filled
import atlas.graphviz.RankDir.TopToBottom
import atlas.graphviz.Shape.Box
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

rootProject.name = "aktual"

apply(from = "gradle/repositories.gradle.kts")

pluginManagement {
  includeBuild("build-logic")

  // Repositories for resolving the plugins {} block below must be declared in a literal
  // pluginManagement block. Gradle evaluates that block before it runs apply(from = ...), so the
  // repos in gradle/repositories.gradle.kts aren't visible here. Only the repos those plugins need:
  // google() for AGP, the portal for everything else.
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    gradlePluginPortal()
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
      mavenContent { snapshotsOnly() }
    }
  }
}

// Included again here (outside pluginManagement) so its artifact is substitutable as a regular
// dependency, not just resolvable as a plugin — needed for build-logic's aktual.compiler Gradle
// plugin, which adds it to consumers' kotlinCompilerPluginClasspath.
includeBuild("compiler-plugin")

plugins {
  id("com.android.application") version "9.4.0" apply false
  id("com.android.kotlin.multiplatform.library") version "9.4.0" apply false
  id("dev.jonpoulton.blueprint") version "2.4.2" apply false
  id("org.jetbrains.kotlin.jvm") version "2.4.10" apply false
  id("org.jetbrains.kotlin.multiplatform") version "2.4.10" apply false

  id("com.autonomousapps.build-health") version "3.19.1"
  id("com.gradle.develocity") version "4.5.0"
  id("dev.jonpoulton.atlas") version "0.5.2"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("org.jetbrains.kotlinx.kover.aggregation") version "0.9.9"
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

develocity.buildScan {
  if (!gradle.startParameter.isBuildScan) {
    publishing.onlyIf { it.isAuthenticated }
  }

  uploadInBackground = false
}

kover {
  enableCoverage()

  reports {
    excludedClasses.addAll("*Application*", "*Preview*Kt*")
    excludedProjects.addAll(":aktual-core:l10n", ":aktual-core:ui")
    excludesAnnotatedBy.addAll(
      "aktual.core.ui.DesktopPreview",
      "aktual.core.ui.LandscapePreview",
      "aktual.core.ui.PortraitPreview",
      "aktual.core.ui.TabletPreview",
      "androidx.compose.runtime.Composable",
      "androidx.compose.ui.tooling.preview.Preview",
      "dev.zacsweers.metro.BindingContainer",
      "javax.annotation.processing.Generated",
    )

    verify.rule {
      groupBy = GroupingEntityType.APPLICATION

      bound {
        minValue = 35
        maxValue = 100
        coverageUnits = CoverageUnit.INSTRUCTION
        aggregationForGroup = AggregationType.COVERED_PERCENTAGE
      }
    }
  }
}

atlas {
  checkOutputs = false
  ignoredConfigs = setOf("debug", "kover", "test", "classpath", "detekt")

  pathTransforms { remove(":aktual-") }

  projectTypes {
    hasPluginId(name = "ViewModel", pluginId = "aktual.module.viewmodel", color = "#914141") // pink
    hasPluginId(name = "DI", pluginId = "aktual.module.di", color = "#a17103") // orange
    hasPluginId(name = "UI", pluginId = "aktual.module.compose", color = "#6b6b01") // yellow
    hasPluginId(name = "Kotlin", pluginId = "aktual.module.kotlin", color = "#160185") // indigo
    pathContains(name = "App", pathContains = ":aktual-app:", color = "#7a0101") // red
    hasPluginId(name = "JVM", pluginId = "aktual.module.jvm", color = "#2f015c") // violet
    other(color = "#808080") // grey
  }

  linkTypes {
    "commonMainApi"(style = Solid, displayName = "api")
    "commonMainImplementation"(style = Dashed, color = "aqua", displayName = "implementation")
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

include(
  ":aktual-about:data",
  ":aktual-about:ui",
  ":aktual-about:vm",
  ":aktual-account:domain",
  ":aktual-account:ui",
  ":aktual-account:vm",
  ":aktual-api",
  ":aktual-api:di",
  ":aktual-api:impl",
  ":aktual-api:model",
  ":aktual-app:android",
  ":aktual-app:desktop",
  ":aktual-app:di",
  ":aktual-app:nav",
  ":aktual-app:ui-app",
  ":aktual-app:ui-budget",
  ":aktual-budget",
  ":aktual-budget:data:db",
  ":aktual-budget:data:encryption",
  ":aktual-budget:data:impl",
  ":aktual-budget:data:proto",
  ":aktual-budget:list:ui",
  ":aktual-budget:list:vm",
  ":aktual-budget:model",
  ":aktual-budget:navrail:ui",
  ":aktual-budget:navrail:vm",
  ":aktual-budget:reports:ui",
  ":aktual-budget:reports:vm",
  ":aktual-budget:rules:ui",
  ":aktual-budget:rules:vm",
  ":aktual-budget:schedules:ui",
  ":aktual-budget:schedules:vm",
  ":aktual-budget:sync:domain",
  ":aktual-budget:sync:ui",
  ":aktual-budget:sync:vm",
  ":aktual-budget:tags:ui",
  ":aktual-budget:tags:vm",
  ":aktual-budget:transactions:ui",
  ":aktual-budget:transactions:vm",
  ":aktual-core",
  ":aktual-core:icons",
  ":aktual-core:l10n",
  ":aktual-core:logging",
  ":aktual-core:logging:impl",
  ":aktual-core:model",
  ":aktual-core:nav",
  ":aktual-core:theme",
  ":aktual-core:theme:impl",
  ":aktual-core:theme:model",
  ":aktual-core:ui",
  ":aktual-di:bindings",
  ":aktual-di:core",
  ":aktual-di:graphs",
  ":aktual-di:runlevel",
  ":aktual-di:runlevel:impl",
  ":aktual-metrics:ui",
  ":aktual-metrics:vm",
  ":aktual-prefs",
  ":aktual-prefs:impl",
  ":aktual-prefs:ui",
  ":aktual-prefs:vm",
  ":aktual-test",
  ":aktual-test:api",
  ":aktual-test:compose",
  ":aktual-test:smoke",
  ":detekt-rules",
)

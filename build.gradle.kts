import aktual.gradle.dsl.androidTestLibraries
import aktual.gradle.dsl.composeLibraries
import aktual.gradle.dsl.testLibraries
import atlas.graphviz.ArrowType.None
import atlas.graphviz.ArrowType.Normal
import atlas.graphviz.FileFormat.Png
import atlas.graphviz.LayoutEngine.Dot
import atlas.graphviz.LinkStyle.Dashed
import atlas.graphviz.LinkStyle.Solid
import atlas.graphviz.NodeStyle.Filled
import atlas.graphviz.RankDir.TopToBottom
import atlas.graphviz.Shape.Box
import com.autonomousapps.extension.Issue

plugins {
  alias(libs.plugins.agp.app) apply false
  alias(libs.plugins.agp.kmp) apply false
  alias(libs.plugins.androidCacheFix) apply false
  alias(libs.plugins.blueprint) apply false
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.burst) apply false
  alias(libs.plugins.catalog) apply false
  alias(libs.plugins.compose) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.licensee) apply false
  alias(libs.plugins.manifestLock) apply false
  alias(libs.plugins.metro) apply false
  alias(libs.plugins.nucleus) apply false
  alias(libs.plugins.redacted) apply false
  alias(libs.plugins.sqldelight) apply false
  alias(libs.plugins.straitjacket) apply false
  alias(libs.plugins.wire) apply false

  alias(libs.plugins.atlas)

  // TODO: reapply. See https://github.com/runningcode/gradle-doctor/issues/481
  // alias(libs.plugins.doctor)

  id("aktual.convention.idea")
}

// doctor {
//   javaHome {
//     ensureJavaHomeMatches = true
//     ensureJavaHomeIsSet = true
//     failOnError = true
//   }
// }

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

dependencyAnalysis {
  useTypesafeProjectAccessors(false)

  reporting { printBuildHealth(false) }

  structure {
    fun composeBundle(name: String) =
      bundle("compose.$name") {
        includeGroup("androidx.compose.$name")
        includeGroup("org.jetbrains.compose.$name")
      }

    fun androidxBundle(name: String) =
      bundle("androidx.$name") {
        includeGroup("androidx.$name")
        includeGroup("org.jetbrains.androidx.$name")
      }

    ignoreKtx(true)

    bundle("haze") { includeGroup("dev.chrisbanes.haze") }
    bundle("kotlin") { includeGroup("org.jetbrains.kotlin") }
    bundle("ktor") { includeGroup("io.ktor") }
    bundle("mockk") { includeGroup("io.mockk") }
    bundle("kotlinx-s13n") {
      includeDependency(libs.kotlinx.serialization.core)
      includeDependency(libs.kotlinx.serialization.json)
    }

    composeBundle("animation")
    composeBundle("foundation")
    composeBundle("material3")
    composeBundle("runtime")
    composeBundle("ui")

    androidxBundle("lifecycle")
    androidxBundle("navigation3")
    androidxBundle("savedstate")
  }

  issues {
    fun Issue.fail() = severity("fail")
    fun Issue.ignore() = severity("ignore")

    all {
      onAny { fail() }
      onRuntimeOnly { ignore() }

      onIncorrectConfiguration {
        exclude(libs.metro.runtime, libs.androidx.test.composeJunit4)
        exclude("org.jetbrains.kotlin:kotlin-test")
      }

      onUsedTransitiveDependencies {
        exclude(libs.logcat)
        excludeRegex(
          // Kotlin stdlib variants - too noisy
          "org\\.jetbrains\\.kotlin:.*",

          // KMP library variants - DAGP has trouble differentiating between mylib and mylib-desktop
          "^.*:.*-android:.*$",
          "^.*:.*-desktop:.*$",

          // ignore module advice, too noisy
          "^:.*$",

          // ¯\_(ツ)_/¯
          "^org.jetbrains:annotations:.*$",
        )
      }

      onUnusedDependencies {
        exclude(*testLibraries.toTypedArray())
        exclude(*androidTestLibraries.toTypedArray())
        exclude(*composeLibraries.toTypedArray())
        exclude(
          libs.alakazam.kotlin,
          libs.androidx.poolingcontainer,
          libs.androidx.savedstate,
          libs.androidx.test.composeJunit4,
          libs.kotlinx.coroutines.core,
          libs.metro.runtime,
          libs.molecule,
        )
        exclude(
          ":aktual-core:logging",
          ":aktual-test",
          ":aktual-test:compose",
        )
        excludeRegex(
          "org\\.jetbrains\\.compose\\.desktop:desktop-jvm-.*",
          "org\\.jetbrains\\.compose\\.hot-reload:.*",
        )
      }
    }

    listOf(
        ":aktual-app:di",
        ":aktual-app:ui-app",
        ":aktual-app:ui-budget",
        ":aktual-test",
        ":aktual-test:compose",
      )
      .forEach { path -> project(path) { onUnusedDependencies { ignore() } } }

    listOf(
        ":aktual-test:api",
        ":aktual-test:compose",
        ":aktual-test:smoke",
      )
      .forEach { path -> project(path) { onUsedTransitiveDependencies { ignore() } } }
  }
}

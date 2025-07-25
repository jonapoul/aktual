package actual.gradle

import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.getLibrary
import blueprint.core.libs
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LintPlugin
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension as KMPExtension

internal fun Project.addAndroidFlag() {
  extensions.findByType(CommonExtension::class)?.apply {
    buildFeatures {
      compose = true
    }
  }
}

internal fun Project.addMetrics() {
  val metricReportDir = project.layout.buildDirectory.dir("compose_metrics")
  val stabilityFile = rootProject.layout.projectDirectory.file("config/compose-stability.conf")

  extensions.configure<ComposeCompilerGradlePluginExtension> {
    metricsDestination.set(metricReportDir)
    reportsDestination.set(metricReportDir)
    stabilityConfigurationFiles.add(stabilityFile)
    targetKotlinPlatforms.addAll(androidJvm)
  }
}

internal fun Project.addLint() {
  val lintChecks = configurations.findByName("lintChecks") ?: run {
    pluginManager.apply(LintPlugin::class)
    configurations.getByName("lintChecks")
  }

  dependencies {
    lintChecks(libs.getLibrary("androidx.compose.lint"))
  }
}

internal fun Project.addComposeBom() {
  val composeBom = dependencies.platform(libs.getLibrary("androidx.compose.bom"))
  extensions.findByType<KMPExtension>()?.apply {
    commonMainDependencies {
      implementation(composeBom)
    }
  } ?: run {
    dependencies {
      "implementation"(composeBom)
    }
  }
}

internal fun Project.addComposeAndroid() {
  val composeJunit4 = libs.getLibrary("test.androidx.compose.ui.junit4")
  val composeManifest = libs.getLibrary("test.androidx.compose.ui.manifest")
  val robolectric = libs.getLibrary("test.robolectric")

  extensions.findByType<KMPExtension>()?.apply {
    sourceSets {
      commonTestDependencies { implementation(composeJunit4) }
      androidUnitTestDependencies { implementation(robolectric) }
//      androidDebugDependencies { implementation(composeManifest) }
    }
  } ?: run {
    dependencies {
      "testImplementation"(composeJunit4)
      "testImplementation"(robolectric)
      "debugImplementation"(composeManifest)
    }
  }
}

internal fun KMPExtension.sourceSets(configure: Action<NamedDomainObjectContainer<KotlinSourceSet>>) =
  (this as ExtensionAware).extensions.configure("sourceSets", configure)

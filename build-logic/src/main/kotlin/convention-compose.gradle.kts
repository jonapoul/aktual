import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("convention-kotlin")
  id("convention-android-base")
}

val libs = the<LibrariesForLibs>()

val ext: CommonExtension<*, *, *, *, *, *> = extensions.findByType<BaseAppModuleExtension>()
  ?: extensions.findByType<LibraryExtension>()
  ?: error("No android extension found in $path")

ext.apply {
  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
  }
}

tasks.withType(KotlinCompile::class.java) {
  kotlinOptions {
    // From https://chrisbanes.me/posts/composable-metrics/
    val propertyRoot = "plugin:androidx.compose.compiler.plugins.kotlin"
    val metricReportDir = project.layout.buildDirectory.dir("compose_metrics").get().asFile
    freeCompilerArgs += listOf("-P", "$propertyRoot:reportsDestination=${metricReportDir.absolutePath}")
    freeCompilerArgs += listOf("-P", "$propertyRoot:metricsDestination=${metricReportDir.absolutePath}")
  }
}

val implementation by configurations
val lintChecks by configurations

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  lintChecks(libs.androidx.compose.lint)
}

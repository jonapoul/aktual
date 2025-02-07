import blueprint.core.rootLocalPropertiesOrNull
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
  alias(libs.plugins.agp.app) apply false
  alias(libs.plugins.agp.lib) apply false
  alias(libs.plugins.androidx.room) apply false
  alias(libs.plugins.androidCacheFix) apply false
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.catalog) apply false
  alias(libs.plugins.dependencyGraph) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.hilt) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.powerAssert) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.ktlint) apply false
  alias(libs.plugins.licensee) apply false
  alias(libs.plugins.licenses) apply false
  alias(libs.plugins.spotless) apply false

  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.dependencyGuard)
  alias(libs.plugins.dependencySort)
  alias(libs.plugins.dependencyVersions)
  alias(libs.plugins.doctor)
  alias(libs.plugins.kover)

  alias(libs.plugins.convention.kover)
}

// Place all local properties in the project-level gradle properties map
rootLocalPropertiesOrNull()?.forEach { (key, value) ->
  ext[key.toString()] = value.toString()
}

dependencyAnalysis {
  structure {
    ignoreKtx(ignore = true)
    bundle(name = "kotlin") { includeGroup("org.jetbrains.kotlin:*") }
    bundle(name = "modules") { include("^:.*\$".toRegex()) }
    bundle(name = "okhttp") { includeGroup(group = "com.squareup.okhttp3") }
    bundle(name = "viewModel") { include(regex = "androidx.lifecycle:lifecycle-viewmodel.*".toRegex()) }
  }

  abi {
    exclusions {
      ignoreInternalPackages()
      ignoreGeneratedCode()
    }
  }

  issues {
    all {
      onAny { severity(value = "fail") }

      onRuntimeOnly { severity(value = "ignore") }

      onIncorrectConfiguration {
        recursiveSubProjects().forEach { exclude(it.path) }
      }

      onUnusedDependencies {
        exclude(
          libs.androidx.compose.ui.tooling,
          libs.androidx.compose.ui.toolingPreview,
          libs.hilt.core,
          libs.test.androidx.monitor,
          libs.timber,
        )
      }

      ignoreSourceSet(
        "androidTest",
      )
    }
  }
}

doctor {
  javaHome {
    ensureJavaHomeMatches = false
    ensureJavaHomeIsSet = true
    failOnError = true
  }
}

dependencyGuard {
  configuration("classpath")
}

tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf { !candidate.version.isStable() && currentVersion.isStable() }
}

private fun String.isStable(): Boolean = listOf("alpha", "beta", "rc").none { contains(it, ignoreCase = true) }

private fun recursiveSubProjects(): Sequence<Project> = subprojects
  .asSequence()
  .flatMap { project -> project.subprojects + project }
  .distinctBy { project -> project.path }

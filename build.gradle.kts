import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
  alias(libs.plugins.agp.app) apply false
  alias(libs.plugins.agp.lib) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.hilt) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kover) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.ktlint) apply false
  alias(libs.plugins.licensee) apply false
  alias(libs.plugins.spotless) apply false

  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.dependencyGuard)
  alias(libs.plugins.dependencyVersions)
  alias(libs.plugins.doctor)

  id("convention-test")
}

dependencyAnalysis {
  issues {
    structure {
      ignoreKtx(ignore = true)
      bundle(name = "kotlin-stdlib") { includeGroup(group = "org.jetbrains.kotlin") }
    }

    all {
      onAny {
        severity(value = "fail")
      }
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

tasks.withType(DependencyUpdatesTask::class.java) {
  rejectVersionIf { !candidate.version.isStable() && currentVersion.isStable() }
}

private fun String.isStable(): Boolean = listOf("alpha", "beta", "rc").none { lowercase().contains(it) }

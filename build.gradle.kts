import blueprint.core.rootLocalPropertiesOrNull
import blueprint.recipes.dependencyVersionsBlueprint

plugins {
  alias(libs.plugins.agp.app) apply false
  alias(libs.plugins.agp.lib) apply false
  alias(libs.plugins.androidCacheFix) apply false
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.burst) apply false
  alias(libs.plugins.catalog) apply false
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
  alias(libs.plugins.manifestLock) apply false
  alias(libs.plugins.poko) apply false
  alias(libs.plugins.sqldelight) apply false

  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.dependencyGuard)
  alias(libs.plugins.dependencySort)
  alias(libs.plugins.dependencyVersions)
  alias(libs.plugins.doctor)
  alias(libs.plugins.kover)

  alias(libs.plugins.convention.idea)
  alias(libs.plugins.convention.kover)
}

// Place all local properties in the project-level gradle properties map
rootLocalPropertiesOrNull()?.forEach { (key, value) ->
  ext[key.toString()] = value.toString()
}

dependencyAnalysis {
  useTypesafeProjectAccessors(true)

  usage {
    analysis {
      checkSuperClasses(true)
    }
  }

  structure {
    ignoreKtx(ignore = true)
    bundle(name = "kotlin") { includeGroup("org.jetbrains.kotlin:*") }
    bundle(name = "modules") { include("^:.*$".toRegex()) }
    bundle(name = "ktor") { includeGroup(group = "io.ktor") }
    bundle(name = "viewModel") { include(regex = "androidx.lifecycle:lifecycle-viewmodel.*".toRegex()) }
  }

  reporting {
    printBuildHealth(true)
    onlyOnFailure(true)
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

      onUnusedDependencies {
        exclude(
          libs.androidx.compose.ui.tooling,
          libs.androidx.compose.ui.toolingPreview,
          libs.hilt.core,
          libs.test.androidx.monitor,
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

dependencyVersionsBlueprint()

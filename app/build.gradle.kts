import blueprint.core.gitVersionCode
import blueprint.core.intProperty
import blueprint.core.javaVersionString
import blueprint.core.rootLocalPropertiesOrNull
import blueprint.core.runGitCommandOrNull
import blueprint.core.stringProperty
import org.gradle.internal.extensions.stdlib.capitalized
import java.time.LocalDate

plugins {
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.agp.app)
  alias(libs.plugins.hilt)
  alias(libs.plugins.licenses)
  alias(libs.plugins.convention.kotlin.jvm)
  alias(libs.plugins.convention.android.base)
  alias(libs.plugins.convention.compose)
  alias(libs.plugins.convention.diagrams)
  alias(libs.plugins.convention.hilt)
  alias(libs.plugins.convention.style)
  alias(libs.plugins.convention.test)
  alias(libs.plugins.dependencySort)
  alias(libs.plugins.dependencyGuard)
}

dependencyGuard {
  configuration("releaseRuntimeClasspath")
}

val gitCommitHash =
  runGitCommandOrNull(args = listOf("rev-parse", "--short=8", "HEAD"))
    ?: error("Failed getting git version from ${project.path}")

fun versionName(): String = with(LocalDate.now()) { "%04d.%02d.%02d".format(year, monthValue, dayOfMonth) }

android {
  namespace = "actual.android.app"
  compileSdk = intProperty(key = "blueprint.android.compileSdk")

  defaultConfig {
    applicationId = "dev.jonpoulton.actual.app"
    minSdk = intProperty(key = "blueprint.android.minSdk")
    targetSdk = intProperty(key = "blueprint.android.targetSdk")
    versionCode = gitVersionCode()
    versionName = versionName()
    multiDexEnabled = true
    setProperty("archivesBaseName", "$applicationId-$versionName")

    val kotlinTime = "kotlinx.datetime.Instant.Companion.fromEpochMilliseconds(${System.currentTimeMillis()}L)"
    buildConfigField("kotlinx.datetime.Instant", "BUILD_TIME", kotlinTime)
    buildConfigField("String", "GIT_HASH", "\"${gitCommitHash}\"")
  }

  kotlinOptions {
    jvmTarget = javaVersionString()
  }

  packaging {
    resources.excludes.addAll(
      listOf(
        "META-INF/DEPENDENCIES",
        "META-INF/INDEX.LIST",
      ),
    )

    jniLibs {
      useLegacyPackaging = true
    }
  }

  buildFeatures {
    buildConfig = true
  }

  signingConfigs {
    val localProps = rootLocalPropertiesOrNull()
    if (localProps != null) {
      create("release") {
        storeFile = rootProject.file(stringProperty(key = "actual.keyFile"))
        storePassword = stringProperty(key = "actual.keyFilePassword")
        keyAlias = stringProperty(key = "actual.keyAlias")
        keyPassword = stringProperty(key = "actual.keyPassword")
      }
    } else {
      logger.error("No local.properties found - skipping signing configs")
    }
  }
}

hilt {
  enableAggregatingTask = true
  enableExperimentalClasspathAggregation = true
}

// Used to populate the licenses screen in-app
licenseReport {
  // Generate JSON and place in app assets
  generateJsonReport = true
  copyJsonReportToAssets = true

  // Disable other report outputs
  generateHtmlReport = false
  generateCsvReport = false
  generateTextReport = false
}

fun registerLicenseTask(suffix: String) {
  val capitalized = suffix.capitalized()
  val assemble = tasks.getByName("assemble$capitalized")
  val license = tasks.getByName("license${capitalized}Report")
  assemble.dependsOn(license)
  license.doFirst {
    val file = project.file("src/main/assets/open_source_licenses.json")
    file.delete()
  }
}

afterEvaluate {
  registerLicenseTask(suffix = "debug")
  registerLicenseTask(suffix = "release")
}

dependencies {
  implementation(libs.alakazam.android.core)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.savedstate)
  implementation(libs.androidx.navigation.commonKtx)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.navigation.runtime)
  implementation(libs.androidx.preference.ktx)
  implementation(libs.androidx.splash)
  implementation(libs.dagger.core)
  implementation(libs.hilt.android)
  implementation(libs.javaxInject)
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.datetime)
  implementation(libs.preferences.android)
  implementation(libs.timber)
  implementation(projects.about.data)
  implementation(projects.about.nav)
  implementation(projects.about.ui)
  implementation(projects.api.core)
  implementation(projects.budget.list.nav)
  implementation(projects.budget.list.ui)
  implementation(projects.core.buildconfig)
  implementation(projects.core.colorscheme)
  implementation(projects.core.coroutines)
  implementation(projects.core.log)
  implementation(projects.licenses.data)
  implementation(projects.licenses.nav)
  implementation(projects.licenses.ui)
  implementation(projects.login.nav)
  implementation(projects.login.ui)
  implementation(projects.url.nav)
  implementation(projects.url.prefs)
  implementation(projects.url.ui)
}

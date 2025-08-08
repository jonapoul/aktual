import blueprint.core.gitVersionCode
import blueprint.core.gitVersionHash
import blueprint.core.intProperty
import blueprint.core.jvmTarget
import blueprint.core.rootLocalPropertiesOrNull
import blueprint.core.stringProperty
import blueprint.core.stringPropertyOrNull
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDate

plugins {
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.agp.app)
  alias(libs.plugins.manifestLock)
  alias(libs.plugins.convention.android.base)
  alias(libs.plugins.convention.compose)
  alias(libs.plugins.convention.di)
  alias(libs.plugins.convention.diagrams)
  alias(libs.plugins.convention.kotlin.jvm)
  alias(libs.plugins.convention.style)
  alias(libs.plugins.convention.test)
  alias(libs.plugins.dependencyGuard)
}

dependencyGuard {
  configuration("releaseRuntimeClasspath")
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget = jvmTarget()
  }
}

val gitCommitHash = project.gitVersionHash()
val gitCode = project.gitVersionCode()

fun versionName(): String = with(LocalDate.now()) { "%04d.%02d.%02d".format(year, monthValue, dayOfMonth) }

android {
  namespace = "actual.app.android"
  compileSdk = intProperty(key = "blueprint.android.compileSdk")

  defaultConfig {
    applicationId = "dev.jonpoulton.actual.app"
    minSdk = intProperty(key = "blueprint.android.minSdk")
    targetSdk = intProperty(key = "blueprint.android.targetSdk")
    versionCode = gitCode
    versionName = versionName()
    multiDexEnabled = true
    base.archivesName = "$applicationId-$versionName"

    val kotlinTime = "kotlin.time.Instant.Companion.fromEpochMilliseconds(${System.currentTimeMillis()}L)"
    buildConfigField("kotlin.time.Instant", "BUILD_TIME", kotlinTime)
    buildConfigField("String", "GIT_HASH", "\"${gitCommitHash}\"")
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
    resValues = true
  }

  signingConfigs {
    val release by creating

    val localProps = rootLocalPropertiesOrNull()
    if (localProps != null) {
      release.apply {
        storeFile = rootProject.file(stringProperty(key = "actual.keyFile"))
        storePassword = stringProperty(key = "actual.keyFilePassword")
        keyAlias = stringProperty(key = "actual.keyAlias")
        keyPassword = stringProperty(key = "actual.keyPassword")
      }
    } else {
      logger.warn("No local.properties found - skipping signing configs")
    }
  }

  buildTypes {
    debug {
      signingConfig = signingConfigs.findByName("debug")
      val defaultUrl = stringPropertyOrNull(key = "actual.defaultUrl")
      val defaultPassword = stringPropertyOrNull(key = "actual.defaultPassword")
      buildConfigField("String", "DEFAULT_URL", if (defaultUrl == null) "null" else "\"${defaultUrl}\"")
      buildConfigField("String", "DEFAULT_PASSWORD", if (defaultPassword == null) "null" else "\"${defaultPassword}\"")
    }

    release {
      signingConfig = signingConfigs.findByName("release")
      buildConfigField("String", "DEFAULT_URL", "null")
      buildConfigField("String", "DEFAULT_PASSWORD", "null")
    }
  }

  manifestLock {
    failOnLockChange = true
    content {
      sdkVersion = true
      configurations = true
      permissions = true
      features = true
      libraries = true
      nativeLibraries = true
      exports = true
    }
  }
}

licensee {
  bundleAndroidAsset = true
}

dependencies {
  implementation(project(":app:di"))
  implementation(project(":app:nav"))
  implementation(project(":modules:budget:di"))
  implementation(project(":modules:core:connection"))
  implementation(project(":modules:core:di"))
  implementation(project(":modules:core:ui"))
  implementation(project(":modules:l10n"))
  implementation(project(":modules:logging"))
  implementation(project(":modules:prefs"))
  implementation(compose.runtime)
  implementation(compose.uiTooling)
  implementation(libs.alakazam.kotlin.compose)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.crypto)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.savedstate)
  implementation(libs.androidx.preference.ktx)
  implementation(libs.androidx.splash)
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.logcat)
  implementation(libs.material)
  implementation(libs.molecule)
  implementation(libs.preferences.android)
}

metro {
  debug = true
}

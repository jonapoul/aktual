import blueprint.core.gitVersionCode
import blueprint.core.gitVersionHash
import blueprint.core.intProperty
import blueprint.core.javaVersionString
import blueprint.core.rootLocalPropertiesOrNull
import blueprint.core.stringProperty
import blueprint.core.stringPropertyOrNull
import java.time.LocalDate

plugins {
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.agp.app)
  alias(libs.plugins.hilt)
  alias(libs.plugins.manifestLock)
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

val gitCommitHash = project.gitVersionHash()
val gitCode = project.gitVersionCode()

fun versionName(): String = with(LocalDate.now()) { "%04d.%02d.%02d".format(year, monthValue, dayOfMonth) }

android {
  namespace = "actual.android.app"
  compileSdk = intProperty(key = "blueprint.android.compileSdk")

  defaultConfig {
    applicationId = "dev.jonpoulton.actual.app"
    minSdk = intProperty(key = "blueprint.android.minSdk")
    targetSdk = intProperty(key = "blueprint.android.targetSdk")
    versionCode = gitCode.toInt()
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
    val debug by getting
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

hilt {
  enableAggregatingTask = true
  enableExperimentalClasspathAggregation = true
}

licensee {
  bundleAndroidAsset = true
}

dependencies {
  implementation(libs.alakazam.android.core)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.alakazam.kotlin.logging)
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
  implementation(projects.about.info.nav)
  implementation(projects.about.info.ui)
  implementation(projects.about.licenses.nav)
  implementation(projects.about.licenses.ui)
  implementation(projects.account.login.domain)
  implementation(projects.account.login.nav)
  implementation(projects.account.login.ui)
  implementation(projects.account.password.nav)
  implementation(projects.account.password.ui)
  implementation(projects.api.di)
  implementation(projects.budget.list.nav)
  implementation(projects.budget.list.ui)
  implementation(projects.budget.sync.nav)
  implementation(projects.budget.sync.ui)
  implementation(projects.core.connection)
  implementation(projects.core.di)
  implementation(projects.db)
  implementation(projects.prefs)
  implementation(projects.settings.nav)
  implementation(projects.settings.ui)
  implementation(projects.url.nav)
  implementation(projects.url.ui)
}

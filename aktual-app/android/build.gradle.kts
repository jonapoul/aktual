import aktual.gradle.ConventionLicensee.Companion.LICENSEE_REPORT_ASSET_NAME
import aktual.gradle.gitVersionCode
import aktual.gradle.intProperty
import aktual.gradle.jvmTarget
import aktual.gradle.localPropertiesOrNull
import aktual.gradle.requireString
import aktual.gradle.versionName

plugins {
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.agp.app)
  alias(libs.plugins.manifestLock)
  alias(libs.plugins.convention.android.base)
  alias(libs.plugins.convention.compose)
  alias(libs.plugins.convention.kotlin.jvm)
  alias(libs.plugins.convention.style)
  alias(libs.plugins.convention.test)
}

kotlin {
  compilerOptions {
    jvmTarget = jvmTarget()
  }
}

android {
  namespace = "aktual.app.android"
  compileSdk = intProperty(key = "aktual.android.compileSdk").get()

  defaultConfig {
    applicationId = "dev.jonpoulton.aktual.app"
    minSdk = intProperty(key = "aktual.android.minSdk").get()
    targetSdk = intProperty(key = "aktual.android.targetSdk").get()
    versionCode = gitVersionCode()
    versionName = versionName()
    multiDexEnabled = true
    base.archivesName = "$applicationId-$versionName"
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
    val localProps = rootProject.localPropertiesOrNull()
    if (localProps != null) {
      release.apply {
        storeFile = rootProject.file(localProps.requireString("aktual.keyFile"))
        storePassword = localProps.requireString("aktual.keyFilePassword")
        keyAlias = localProps.requireString("aktual.keyAlias")
        keyPassword = localProps.requireString("aktual.keyPassword")
      }
    } else {
      logger.warn("No local.properties found - skipping signing configs")
    }
  }

  buildTypes {
    debug {
      signingConfig = signingConfigs.findByName("debug")
    }

    release {
      signingConfig = signingConfigs.findByName("release")
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
  androidAssetReportPath = LICENSEE_REPORT_ASSET_NAME
}

dependencies {
  implementation(project(":aktual-app:di"))
  implementation(project(":aktual-app:nav"))
  implementation(project(":aktual-prefs"))
  implementation(libs.alakazam.kotlin.compose)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.savedstate)
  implementation(libs.androidx.splash)
  implementation(libs.jetbrains.uiTooling)
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logcat)
  implementation(libs.material)
}

val exportMinSdk by tasks.registering {
  group = "documentation"
  description = "Updates the API level badge in README.md"
  val minSdk = android.defaultConfig.minSdk
  val readmeFile = rootDir.resolve("README.md")
  doLast {
    val originalContent = readmeFile.readText()
    val newContent = originalContent
      .replace("API-\\d+%2B".toRegex(), "API-$minSdk%2B")
      .replace("level=\\d+".toRegex(), "level=$minSdk")
    readmeFile.writeText(newContent)
    if (originalContent != newContent) {
      throw GradleException("Updated $readmeFile with minSdk=$minSdk - you need to commit it!")
    }
  }
}

tasks.named("preBuild").configure { dependsOn(exportMinSdk) }

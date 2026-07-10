@file:Suppress("UnstableApiUsage")

import aktual.gradle.ConventionLicensee.Companion.LICENSEE_REPORT_ASSET_NAME
import aktual.gradle.ExportMinSdkTask
import aktual.gradle.dsl.commonConfigure
import blueprint.core.getOptional
import blueprint.core.gitVersionCode
import blueprint.core.gitVersionDate
import blueprint.core.intProperty
import blueprint.core.javaVersion
import blueprint.core.localProperties

plugins {
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.agp.app)
  alias(libs.plugins.manifestLock)
  alias(libs.plugins.androidCacheFix)
  id("aktual.convention.compose")
  id("aktual.convention.kotlin")
  id("aktual.convention.style")
  id("aktual.convention.test")
}

val gitVersionCode = gitVersionCode()
val gitVersionName = gitVersionDate()

android {
  namespace = "aktual.app.android"
  compileSdk = providers.intProperty(key = "aktual.android.compileSdk").get()

  defaultConfig {
    applicationId = "dev.jonpoulton.aktual.app"
    minSdk = providers.intProperty(key = "aktual.android.minSdk").get()
    targetSdk = providers.intProperty(key = "aktual.android.targetSdk").get()
    versionCode = gitVersionCode.get()
    versionName = gitVersionName.get()
    multiDexEnabled = true
    base.archivesName = "$applicationId-$versionName"
    testInstrumentationRunnerArguments["disableAnalytics"] = "true"
  }

  val version = javaVersion()
  compileOptions.apply {
    sourceCompatibility = version.get()
    targetCompatibility = version.get()
    isCoreLibraryDesugaringEnabled = true
  }

  lint.commonConfigure(project)

  packaging {
    resources.excludes += setOf("**/native/Windows/**", "**/native/Mac/**")

    jniLibs { useLegacyPackaging = true }
  }

  buildFeatures {
    buildConfig = true
    compose = true
    resValues = true

    // Disable useless build steps
    aidl = false
    prefab = false
    shaders = false
    viewBinding = false
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      isReturnDefaultValues = true
    }
  }

  signingConfigs {
    register("release") {
      with(localProperties()) {
        storeFile =
          getOptional("aktual.keyFile")?.let {
            rootProject.isolated.projectDirectory.file(it).asFile
          }
        storePassword = getOptional("aktual.keyFilePassword")
        keyAlias = getOptional("aktual.keyAlias")
        keyPassword = getOptional("aktual.keyPassword")
      }
    }
  }

  buildTypes {
    debug {
      applicationIdSuffix = ".dev"
      versionNameSuffix = "-dev"
      signingConfig = signingConfigs.findByName("debug")
      isMinifyEnabled = false
    }

    release {
      signingConfig = signingConfigs.findByName("release")
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        projectDir.resolve("proguard-rules.pro"),
        layout.projectDirectory.file("../common-rules.pro"),
      )
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

androidComponents {
  // disable instrumented tests if the relevant folder doesn't exist
  beforeVariants { variant ->
    variant.enableAndroidTest =
      variant.enableAndroidTest && projectDir.resolve("src/androidTest").exists()
  }

  onVariants(selector().withBuildType("release")) { variant ->
    variant.packaging.resources.excludes.add("META-INF/*")
    variant.outputs.forEach { output ->
      output.outputFileName =
        output.versionName.map { version -> "aktual.$version.${variant.buildType}.apk" }
    }
  }
}

licensee {
  bundleAndroidAsset = true
  androidAssetReportPath = LICENSEE_REPORT_ASSET_NAME
}

dependencies {
  coreLibraryDesugaring(libs.android.desugaring)
  implementation(libs.alakazam.compose)
  implementation(libs.alakazam.kotlin)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.activity.core)
  implementation(libs.androidx.activity.ktx)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.lifecycle.viewmodel.savedstate)
  implementation(libs.androidx.splash)
  implementation(libs.haze)
  implementation(libs.kotlinx.coroutines.core)
  debugImplementation(libs.leakcanary)
  implementation(libs.logcat)
  implementation(libs.material)
  implementation(libs.metrox.android)
  implementation(libs.metrox.viewmodel.compose)
  implementation(project(":aktual-about:ui"))
  implementation(project(":aktual-app:di"))
  implementation(project(":aktual-app:nav"))
  implementation(project(":aktual-app:ui-app"))
  implementation(project(":aktual-app:ui-budget"))
  implementation(project(":aktual-core:nav"))
  implementation(project(":aktual-di:graphs"))
  implementation(project(":aktual-prefs"))
}

val readme = rootProject.layout.projectDirectory.file("README.md")
val exportMinSdk =
  tasks.register("exportMinSdk", ExportMinSdkTask::class) {
    minSdk = providers.intProperty(key = "aktual.android.minSdk")
    readmeFile = readme
    outputFile = readme
  }

tasks.named("preBuild").configure { dependsOn(exportMinSdk) }

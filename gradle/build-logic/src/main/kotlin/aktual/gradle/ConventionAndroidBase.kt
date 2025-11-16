package aktual.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasAndroidTestBuilder
import org.gradle.android.AndroidCacheFixPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

class ConventionAndroidBase : Plugin<Project> {
  @Suppress("LongMethod")
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(AndroidCacheFixPlugin::class)
    }

    extensions.configure(CommonExtension::class) {
      // ":aktual-path:to:module" -> "aktual.path.to.module", or ":aktual-app:android" -> "aktual.app.android"
      namespace = path
        .split(":", "-")
        .filter { it.isNotBlank() }
        .joinToString(".")

      compileSdk = intProperty("aktual.android.compileSdk").get()

      defaultConfig {
        minSdk = intProperty("aktual.android.minSdk").get()
        testInstrumentationRunnerArguments["disableAnalytics"] = "true"
      }

      extensions.findByType(KotlinJvmCompilerOptions::class)?.apply {
        jvmTarget.set(project.jvmTarget())
      }

      val version = javaVersion()
      compileOptions {
        sourceCompatibility = version
        targetCompatibility = version
      }

      buildFeatures {
        // Enabled in modules that need them
        resValues = false
        viewBinding = false

        // Disable useless build steps
        aidl = false
        buildConfig = false
        compose = false
        prefab = false
        renderScript = false
        shaders = false
      }

      testOptions {
        unitTests {
          isIncludeAndroidResources = true
          isReturnDefaultValues = true
        }
      }

      extensions.findByType(AndroidComponentsExtension::class)?.apply {
        // disable instrumented tests if androidTest folder doesn't exist
        beforeVariants {
          if (it is HasAndroidTestBuilder) {
            it.enableAndroidTest = it.enableAndroidTest && projectDir.resolve("src/androidTest").exists()
          }
        }
      }

      lint {
        abortOnError = false
        checkGeneratedSources = false
        checkReleaseBuilds = false
        checkReleaseBuilds = false
        checkTestSources = true
        explainIssues = true
        htmlReport = true
        xmlReport = true
        lintConfig = rootProject.file("config/lint.xml")
      }

      compileOptions {
        isCoreLibraryDesugaringEnabled = true
      }
    }

    dependencies {
      "coreLibraryDesugaring"(libs["android.desugaring"])
    }
  }
}

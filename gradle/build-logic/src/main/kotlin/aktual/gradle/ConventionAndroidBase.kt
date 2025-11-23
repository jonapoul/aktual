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

class ConventionAndroidBase : Plugin<Project> {
  @Suppress("LongMethod")
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(AndroidCacheFixPlugin::class)
    }

    extensions.configure(CommonExtension::class) {
      namespace = buildNamespace()
      compileSdk = intProperty("aktual.android.compileSdk").get()

      defaultConfig {
        minSdk = intProperty("aktual.android.minSdk").get()
        testInstrumentationRunnerArguments["disableAnalytics"] = "true"
      }

      compileOptions {
        val version = javaVersion().get()
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

      extensions.configure(AndroidComponentsExtension::class) {
        // disable instrumented tests if androidTest folder doesn't exist
        beforeVariants { variant ->
          if (variant is HasAndroidTestBuilder) {
            variant.enableAndroidTest = variant.enableAndroidTest && projectDir.resolve("src/androidTest").exists()
          }
        }
      }

      lint.commonConfigure(target)
      packaging.commonConfigure()

      compileOptions {
        isCoreLibraryDesugaringEnabled = true
      }
    }

    dependencies {
      "coreLibraryDesugaring"(libs["android.desugaring"])
    }
  }
}

@file:Suppress("UnstableApiUsage", "LongMethod")

package aktual.gradle

import blueprint.core.get
import blueprint.core.intProperty
import blueprint.core.libs
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
  override fun apply(target: Project): Unit =
    with(target) {
      with(pluginManager) { apply(AndroidCacheFixPlugin::class) }

      extensions.configure(CommonExtension::class) {
        namespace = buildNamespace()
        compileSdk = providers.intProperty("aktual.android.compileSdk").get()

        defaultConfig.apply {
          minSdk = providers.intProperty("aktual.android.minSdk").get()
          testInstrumentationRunnerArguments["disableAnalytics"] = "true"
        }

        val version = javaVersion()
        compileOptions.apply {
          sourceCompatibility = version.get()
          targetCompatibility = version.get()
          isCoreLibraryDesugaringEnabled = true
        }

        buildFeatures.apply {
          // Enabled in modules that need them
          resValues = false
          viewBinding = false

          // Disable useless build steps
          aidl = false
          buildConfig = false
          compose = false
          prefab = false
          shaders = false
        }

        testOptions.apply {
          unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
          }
        }

        lint.commonConfigure(target)
        packaging.commonConfigure()
      }

      extensions.configure(AndroidComponentsExtension::class) {
        // disable instrumented tests if the relevant folder doesn't exist
        beforeVariants { variant ->
          if (variant is HasAndroidTestBuilder) {
            val testDirExists =
              projectDir.resolve("src/androidDeviceTest").exists() || // AGP-KMP
                projectDir.resolve("src/androidTest").exists() // Regular AGP
            variant.enableAndroidTest = variant.enableAndroidTest && testDirExists
          }
        }
      }

      dependencies { "coreLibraryDesugaring"(libs["android.desugaring"]) }
    }
}

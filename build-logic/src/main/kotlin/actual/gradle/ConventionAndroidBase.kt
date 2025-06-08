@file:Suppress("UnstableApiUsage")

package actual.gradle

import blueprint.core.getVersion
import blueprint.core.libs
import blueprint.recipes.androidBaseBlueprint
import blueprint.recipes.androidDesugaringBlueprint
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

class ConventionAndroidBase : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    androidBaseBlueprint()

    androidDesugaringBlueprint(libs.getVersion("android.desugaring"))

    extensions.findByType(CommonExtension::class)?.apply {
      // ":modules:path:to:module" -> "actual.path.to.module", or ":app" -> "actual.app"
      namespace = "actual" + path.removePrefix(":modules").split(":").joinToString(".")

      defaultConfig {
        testInstrumentationRunner = "actual.test.ActualHiltTestRunner"
      }

      testOptions {
        unitTests {
          // For Robolectric
          isIncludeAndroidResources = true
        }
      }
    }
  }
}

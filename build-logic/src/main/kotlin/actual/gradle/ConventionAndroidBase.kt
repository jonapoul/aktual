@file:Suppress("UnstableApiUsage")

package actual.gradle

import blueprint.core.getVersion
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
      // e.g. "actual.path.to.my.module"
      namespace = "actual" + path.split(":").joinToString(".")

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

@file:Suppress("UnstableApiUsage")

package aktual.gradle

import blueprint.core.invoke
import blueprint.core.libs
import blueprint.recipes.androidBaseBlueprint
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

class ConventionAndroidBase : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    androidBaseBlueprint()

    extensions.findByType(CommonExtension::class)?.apply {
      // ":modules:path:to:module" -> "aktual.path.to.module", or ":app-android" -> "aktual.app.android"
      namespace = "aktual" + path
        .removePrefix(":modules")
        .split(":", "-")
        .joinToString(".")

      testOptions {
        unitTests {
          // For Robolectric
          isIncludeAndroidResources = true
        }
      }

      lint {
        lintConfig = rootProject.file("config/lint.xml")
      }

      compileOptions {
        isCoreLibraryDesugaringEnabled = true
      }
    }

    dependencies {
      "coreLibraryDesugaring"(libs("android.desugaring"))
    }
  }
}

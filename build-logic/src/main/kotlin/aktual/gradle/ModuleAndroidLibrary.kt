@file:Suppress("UnstableApiUsage")

package aktual.gradle

import aktual.gradle.dsl.androidTestLibraries
import aktual.gradle.dsl.apply
import aktual.gradle.dsl.buildNamespace
import aktual.gradle.dsl.commonConfigure
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.testLibraries
import blueprint.core.intProperty
import blueprint.core.javaVersion
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke

// A plain (non-KMP) android library, for code that only exists on android
class ModuleAndroidLibrary : ProjectPlugin {
  override fun Project.applyTo() {
    with(pluginManager) {
      apply(LibraryPlugin::class)
      apply(ConventionKotlinJvm::class)
      apply(ConventionIdea::class)
      apply(ConventionStyle::class)
      apply(ConventionTest::class)
    }

    extensions.configure(LibraryExtension::class) {
      namespace = buildNamespace()
      compileSdk = providers.intProperty("aktual.android.compileSdk").get()
      defaultConfig.minSdk = providers.intProperty("aktual.android.minSdk").get()
      packaging.resources.excludes.add("META-INF/*")
      lint.commonConfigure(this@applyTo)

      val javaVersion = javaVersion()
      compileOptions.sourceCompatibility = javaVersion.get()
      compileOptions.targetCompatibility = javaVersion.get()

      with(testOptions.unitTests) {
        // For Robolectric
        isIncludeAndroidResources = true

        // Android stubs return defaults instead of throwing, for tests not using Robolectric
        isReturnDefaultValues = true
      }
    }

    dependencies {
      testLibraries.forEach { lib -> "testImplementation"(lib) }
      androidTestLibraries.forEach { lib -> "testImplementation"(lib) }

      // Pins Robolectric to a supported SDK, see gradle/robolectric/robolectric.properties
      "testImplementation"(files(rootProject.isolated.projectDirectory.dir("gradle/robolectric")))
    }
  }
}

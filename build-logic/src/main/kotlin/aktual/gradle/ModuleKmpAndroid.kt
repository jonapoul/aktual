@file:Suppress("UnstableApiUsage")

package aktual.gradle

import aktual.gradle.dsl.androidTestLibraries
import aktual.gradle.dsl.apply
import aktual.gradle.dsl.buildNamespace
import aktual.gradle.dsl.commonConfigure
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.kotlin
import blueprint.core.intProperty
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.LintPlugin
import com.android.build.gradle.api.KotlinMultiplatformAndroidPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.androidHostTestDependencies

class ModuleKmpAndroid : ProjectPlugin {
  override fun Project.applyTo() {
    with(pluginManager) {
      apply(ModuleKotlin::class)
      apply(KotlinMultiplatformAndroidPlugin::class)
      apply(LintPlugin::class)
    }

    kotlin {
      extensions.configure(KotlinMultiplatformAndroidLibraryTarget::class) {
        namespace = buildNamespace()
        minSdk = providers.intProperty("aktual.android.minSdk").get()
        compileSdk = providers.intProperty("aktual.android.compileSdk").get()
        packaging.resources.excludes.add("META-INF/*")
        lint.commonConfigure(this@applyTo)
        withHostTest {
          // For Robolectric
          isIncludeAndroidResources = true

          // Android stubs return defaults instead of throwing, for tests not using Robolectric
          isReturnDefaultValues = true
        }
      }

      // Pin Robolectric to a supported SDK via a shared robolectric.properties on the
      // androidHostTest classpath (see gradle/robolectric/robolectric.properties)
      sourceSets.named("androidHostTest") { ss ->
        ss.resources.srcDir(rootProject.isolated.projectDirectory.dir("gradle/robolectric"))
      }

      androidHostTestDependencies {
        androidTestLibraries.forEach(::implementation)
        implementation(project(":aktual-test:android"))
      }
    }
  }
}

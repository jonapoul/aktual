package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.provideDelegate

class ConventionTestDependencies : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    val testImplementation = configurations.findByName("testImplementation")
    val isAndroid = project.plugins.any { it is AndroidBasePlugin }

    dependencies {
      testImplementation?.let { testImplementation ->
        testLibraries.forEach { lib -> testImplementation(lib) }

        if (isAndroid) {
          testImplementation(libs.getLibrary("test.androidx.arch"))
          testImplementation(libs.getLibrary("test.androidx.coreKtx"))
          testImplementation(libs.getLibrary("test.androidx.junit"))
          testImplementation(libs.getLibrary("test.androidx.rules"))
          testImplementation(libs.getLibrary("test.androidx.runner"))
          testImplementation(libs.getLibrary("test.mockk.android"))
          testImplementation(libs.getLibrary("test.robolectric"))

          val debugImplementation by configurations
          debugImplementation(libs.getLibrary("test.androidx.monitor"))
          debugImplementation(project(":modules:test:hilt"))
        }
      }
    }
  }
}

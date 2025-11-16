package aktual.gradle

import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ConventionTestDependencies : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    val testImplementation = configurations.findByName("testImplementation")
    val isAndroid = project.plugins.any { it is AndroidBasePlugin }

    dependencies {
      testImplementation?.let { testImplementation ->
        testLibraries.forEach { lib -> testImplementation(lib) }

        if (isAndroid) {
          androidTestLibraries.forEach { lib -> testImplementation(lib) }
          "debugImplementation"(libs["test.androidx.monitor"])
        }
      }
    }
  }
}

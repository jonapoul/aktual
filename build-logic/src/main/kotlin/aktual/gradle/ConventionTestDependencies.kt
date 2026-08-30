package aktual.gradle

import aktual.gradle.dsl.androidTestLibraries
import aktual.gradle.dsl.dependencies
import aktual.gradle.dsl.invoke
import aktual.gradle.dsl.testLibraries
import blueprint.core.get
import blueprint.core.libs
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Project

class ConventionTestDependencies : ProjectPlugin {
  override fun Project.applyTo() {
    if ("testImplementation" !in configurations.names) return
    val isAndroid = project.plugins.any { it is AndroidBasePlugin }

    dependencies {
      testLibraries.forEach { lib -> "testImplementation"(lib) }

      if (isAndroid) {
        androidTestLibraries.forEach { lib -> "testImplementation"(lib) }
        "debugImplementation"(libs["androidx.test.monitor"])
      }
    }
  }
}

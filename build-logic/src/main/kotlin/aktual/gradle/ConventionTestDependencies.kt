package aktual.gradle

import aktual.gradle.dsl.androidTestLibraries
import aktual.gradle.dsl.dependencies
import aktual.gradle.dsl.invoke
import aktual.gradle.dsl.testLibraries
import blueprint.core.get
import blueprint.core.libs
import blueprint.core.withAnyId
import org.gradle.api.Project

class ConventionTestDependencies : ProjectPlugin {
  override fun Project.applyTo() {
    pluginManager.withAnyId("org.jetbrains.kotlin.jvm", "org.jetbrains.kotlin.android") {
      dependencies {
        testLibraries.forEach { lib -> "testImplementation"(lib) }
      }
    }

    pluginManager.withPlugin("com.android.base") {
      dependencies {
        androidTestLibraries.forEach { lib -> "testImplementation"(lib) }
        "debugImplementation"(libs["androidx.test.monitor"])
      }
    }
  }
}

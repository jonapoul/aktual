package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.commonConfigure
import aktual.gradle.dsl.dependencies
import aktual.gradle.dsl.invoke
import aktual.gradle.dsl.testLibraries
import com.android.build.api.dsl.Lint
import com.android.build.gradle.LintPlugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

class ModuleJvm : ProjectPlugin {
  override fun Project.applyTo() {
    with(pluginManager) {
      apply(KotlinPluginWrapper::class)
      apply(ConventionKotlinJvm::class)
      apply(ConventionIdea::class)
      apply(ConventionStyle::class)
      apply(ConventionTest::class)
      apply(LintPlugin::class)
    }

    extensions.configure(Lint::class.java) { lint -> lint.commonConfigure(this@applyTo) }

    dependencies {
      testLibraries.forEach { lib -> "testImplementation"(lib) }
    }
  }
}

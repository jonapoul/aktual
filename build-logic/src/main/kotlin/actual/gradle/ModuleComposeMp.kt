package actual.gradle

import blueprint.core.commonTestDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ModuleComposeMp : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class.java)
      apply(ConventionComposeMp::class.java)
    }

    extensions.configure<KotlinMultiplatformExtension> {
      commonTestDependencies {
        implementation(project(":modules:test:compose"))
      }
    }
  }
}

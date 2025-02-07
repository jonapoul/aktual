package actual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlinBase : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    tasks.withType<KotlinCompile> {
      compilerOptions {
        freeCompilerArgs.addAll(
          "-Xjvm-default=all-compatibility",
          "-opt-in=kotlin.RequiresOptIn",
        )
      }
    }
  }
}

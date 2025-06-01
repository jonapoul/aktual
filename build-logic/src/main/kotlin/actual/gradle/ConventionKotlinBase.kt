package actual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlinBase : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    tasks.withType<KotlinCompile>().configureEach {
      compilerOptions {
        freeCompilerArgs.addAll(FREE_COMPILER_ARGS)
      }
    }
  }
}

val FREE_COMPILER_ARGS = listOf(
  "-Xjvm-default=all-compatibility",
  "-opt-in=kotlin.RequiresOptIn",
  "-opt-in=kotlin.uuid.ExperimentalUuidApi",
  "-opt-in=kotlin.io.encoding.ExperimentalEncodingApi",
)

package actual.gradle

import dev.drewhamilton.poko.gradle.PokoGradlePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlinBase : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(PokoGradlePlugin::class)
    }

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
)

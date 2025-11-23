@file:Suppress("ktlint:standard:max-line-length")

package aktual.gradle

import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.AndroidLintGlobalTask
import com.android.build.gradle.internal.lint.AndroidLintTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlinBase : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ConventionDi::class)
    }

    extensions.configure(HasConfigurableKotlinCompilerOptions::class) {
      compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.addAll(FREE_COMPILER_ARGS)
      }
    }

    pluginManager.withPlugin("com.android.lint") {
      disableTasksOfType<AndroidLintAnalysisTask>()
      disableTasksOfType<AndroidLintGlobalTask>()
      disableTasksOfType<AndroidLintTask>()
    }

    val compileTasks = tasks.withType(KotlinCompile::class.java)
    tasks.register("compileAll") { dependsOn(compileTasks) }
  }

  private inline fun <reified T : Task> Project.disableTasksOfType() {
    tasks.withType(T::class.java).configureEach { enabled = false }
  }

  private companion object {
    val FREE_COMPILER_ARGS = listOf(
      "-Xcontext-parameters", // https://kotlinlang.org/docs/whatsnew22.html#preview-of-context-parameters
      "-Xcontext-sensitive-resolution", // https://kotlinlang.org/docs/whatsnew22.html#preview-of-context-sensitive-resolution
      "-Xexpect-actual-classes",
      "-Xnested-type-aliases", // https://kotlinlang.org/docs/whatsnew22.html#support-for-nested-type-aliases
      "-opt-in=kotlin.RequiresOptIn",
      "-opt-in=kotlin.io.encoding.ExperimentalEncodingApi",
      "-opt-in=kotlin.time.ExperimentalTime",
      "-opt-in=kotlin.uuid.ExperimentalUuidApi",
    )
  }
}

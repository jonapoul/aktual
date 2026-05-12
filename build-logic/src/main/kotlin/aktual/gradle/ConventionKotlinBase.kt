package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.withType
import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import com.github.gmazzo.buildconfig.BuildConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionKotlinBase : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply(ConventionDi::class)

      extensions.configure(HasConfigurableKotlinCompilerOptions::class) {
        compilerOptions {
          allWarningsAsErrors.set(true)
          freeCompilerArgs.addAll(FREE_COMPILER_ARGS)
        }
      }

      val compileTasks = tasks.withType(KotlinCompile::class)
      tasks.register("compileAll") { t -> t.dependsOn(compileTasks) }

      pluginManager.withPlugin("com.github.gmazzo.buildconfig") {
        // Needed because of awkwardness with buildconfig in combo with android lint, see:
        //   - https://github.com/gmazzo/gradle-buildconfig-plugin/issues/342
        //   - https://github.com/gmazzo/gradle-buildconfig-plugin/issues/67#issuecomment-1858603989
        val buildConfig = tasks.withType(BuildConfigTask::class)
        tasks.withType(AndroidLintAnalysisTask::class).configureEach { t ->
          t.mustRunAfter(buildConfig)
        }
        tasks.withType(LintModelWriterTask::class).configureEach { t ->
          t.mustRunAfter(buildConfig)
        }
      }
    }

  private companion object {
    val FREE_COMPILER_ARGS =
      listOf(
        "-Xcontext-parameters", // https://kotlinlang.org/docs/whatsnew22.html#preview-of-context-parameters
        "-Xcontext-sensitive-resolution", // https://kotlinlang.org/docs/whatsnew22.html#preview-of-context-sensitive-resolution
        "-Xexpect-actual-classes",
        "-opt-in=kotlin.RequiresOptIn",
        "-opt-in=kotlin.contracts.ExperimentalContracts",
        "-opt-in=kotlin.uuid.ExperimentalUuidApi",
      )
  }
}

@file:Suppress("ktlint:standard:max-line-length")

package actual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions

class ConventionKotlinBase : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ConventionDi::class)
    }

    extensions.configure(HasConfigurableKotlinCompilerOptions::class) {
      compilerOptions { configure() }
    }
  }

  private fun KotlinCommonCompilerOptions.configure() {
    allWarningsAsErrors.set(true)
    freeCompilerArgs.addAll(FREE_COMPILER_ARGS)
  }
}

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

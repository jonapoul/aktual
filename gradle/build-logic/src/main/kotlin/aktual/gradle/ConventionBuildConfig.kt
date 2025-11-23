package aktual.gradle

import com.android.build.gradle.internal.lint.AndroidLintAnalysisTask
import com.android.build.gradle.internal.lint.LintModelWriterTask
import com.github.gmazzo.buildconfig.BuildConfigPlugin
import com.github.gmazzo.buildconfig.BuildConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType

class ConventionBuildConfig : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(BuildConfigPlugin::class)
    }

    // Needed because of awkwardness with buildconfig in combo with android lint, see:
    //   - https://github.com/gmazzo/gradle-buildconfig-plugin/issues/342
    //   - https://github.com/gmazzo/gradle-buildconfig-plugin/issues/67#issuecomment-1858603989
    val buildConfig = tasks.withType(BuildConfigTask::class.java)
    tasks.withType<AndroidLintAnalysisTask>().configureEach { mustRunAfter(buildConfig) }
    tasks.withType<LintModelWriterTask>().configureEach { mustRunAfter(buildConfig) }
  }
}

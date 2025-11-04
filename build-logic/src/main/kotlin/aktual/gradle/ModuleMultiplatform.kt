package aktual.gradle

import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonTestDependencies
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.KotlinMultiplatformAndroidPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

internal fun ExtensionAware.kotlin(action: KotlinMultiplatformExtension.() -> Unit) =
  extensions.configure<KotlinMultiplatformExtension>(action)

class ModuleMultiplatform : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(KotlinMultiplatformPluginWrapper::class)
      apply(KotlinMultiplatformAndroidPlugin::class)
      apply(ConventionKotlinBase::class)
      apply(ConventionIdea::class)
      apply(ConventionKover::class)
      apply(ConventionStyle::class)
      apply(ConventionTest::class)
      apply(ConventionTest::class)
    }

    kotlin {
      jvm()

      extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
        namespace = buildNamespace()
        lint.lintConfig = rootProject.file("config/lint.xml")
        packaging.configurePackaging()
      }

      commonTestDependencies {
        testLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":modules:test:di"))
        implementation(project(":modules:test:kotlin"))
      }

      androidUnitTestDependencies {
        androidTestLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":modules:test:android"))
      }
    }
  }
}

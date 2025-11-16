package aktual.gradle

import androidUnitTestDependencies
import commonTestDependencies
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.KotlinMultiplatformAndroidPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

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
        minSdk = intProperty("blueprint.android.minSdk")
        compileSdk = intProperty("blueprint.android.compileSdk")
        lint.lintConfig = rootProject.file("config/lint.xml")
        packaging.configurePackaging()
        withHostTest {
          // For Robolectric
          isIncludeAndroidResources = true
        }
      }

      commonTestDependencies {
        testLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":aktual-test:di"))
        implementation(project(":aktual-test:kotlin"))
      }

      androidUnitTestDependencies {
        androidTestLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":aktual-test:android"))
      }
    }
  }
}

package aktual.gradle

import androidHostTestDependencies
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.KotlinMultiplatformAndroidPlugin
import commonMainDependencies
import commonTestDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
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
        minSdk = intProperty("aktual.android.minSdk").get()
        compileSdk = intProperty("aktual.android.compileSdk").get()
        lint.lintConfig = rootProject.file("config/lint.xml")
        packaging.configurePackaging()
        withHostTest {
          // For Robolectric
          isIncludeAndroidResources = true
        }
      }

      compilerOptions {
        freeCompilerArgs.addAll(
          "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
      }

      commonMainDependencies {
        implementation(libs["kotlinx.coroutines.core"])
      }

      commonTestDependencies {
        testLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":aktual-test:di"))
        implementation(project(":aktual-test:kotlin"))
      }

      androidHostTestDependencies {
        androidTestLibraries.forEach { lib -> implementation(lib) }
        implementation(project(":aktual-test:android"))
      }
    }
  }
}

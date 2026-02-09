package aktual.gradle

import androidHostTestDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.get
import blueprint.core.intProperty
import blueprint.core.libs
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.KotlinMultiplatformAndroidPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

class ModuleMultiplatform : Plugin<Project> {
  override fun apply(target: Project): Unit =
      with(target) {
        with(pluginManager) {
          apply(KotlinMultiplatformPluginWrapper::class)
          apply(KotlinMultiplatformAndroidPlugin::class)
          apply(ConventionKotlinBase::class)
          apply(ConventionIdea::class)
          apply(ConventionKover::class)
          apply(ConventionStyle::class)
          apply(ConventionTest::class)
        }

        kotlin {
          applyDefaultHierarchyTemplate()

          jvm()

          extensions.configure(KotlinMultiplatformAndroidLibraryTarget::class) {
            namespace = buildNamespace()
            minSdk = providers.intProperty("aktual.android.minSdk").get()
            compileSdk = providers.intProperty("aktual.android.compileSdk").get()
            packaging.commonConfigure()
            lint.commonConfigure(target)
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
            implementation(libs["alakazam.kotlin"])
            implementation(libs["kotlinx.coroutines.core"])
          }

          commonTestDependencies {
            testLibraries.forEach { lib -> implementation(lib) }
            implementation(project(":aktual-test:kotlin"))
          }

          androidHostTestDependencies {
            androidTestLibraries.forEach { lib -> implementation(lib) }
            implementation(project(":aktual-test:android"))
          }
        }
      }
}

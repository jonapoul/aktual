package aktual.gradle

import aktual.gradle.dsl.androidHostTestDependencies
import aktual.gradle.dsl.androidTestLibraries
import aktual.gradle.dsl.apply
import aktual.gradle.dsl.buildNamespace
import aktual.gradle.dsl.commonConfigure
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.kotlin
import aktual.gradle.dsl.testLibraries
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.get
import blueprint.core.intProperty
import blueprint.core.libs
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.android.build.gradle.api.KotlinMultiplatformAndroidPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

class ModuleKotlin : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      with(pluginManager) {
        apply(KotlinMultiplatformPluginWrapper::class)
        apply(KotlinMultiplatformAndroidPlugin::class)
        apply(ConventionKotlinBase::class)
        apply(ConventionIdea::class)
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
          packaging.resources.excludes.add("META-INF/*")
          lint.commonConfigure(target)
          withHostTest {
            // For Robolectric
            isIncludeAndroidResources = true
          }
        }

        compilerOptions {
          freeCompilerArgs.addAll("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        commonMainDependencies {
          implementation(libs["alakazam.kotlin"])
          implementation(libs["kotlinx.coroutines.core"])
        }

        commonTestDependencies {
          implementation(kotlin("test"))
          testLibraries.forEach { lib -> implementation(lib) }
        }

        androidHostTestDependencies { androidTestLibraries.forEach { lib -> implementation(lib) } }
      }
    }
}

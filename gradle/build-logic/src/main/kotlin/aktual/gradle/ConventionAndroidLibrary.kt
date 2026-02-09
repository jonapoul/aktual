package aktual.gradle

import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class ConventionAndroidLibrary : Plugin<Project> {
  override fun apply(target: Project) =
      with(target) {
        with(pluginManager) {
          apply(LibraryPlugin::class)
          apply(ConventionAndroidBase::class)
        }

        extensions.configure<LibraryExtension> {
          androidResources { enable = false }

          buildFeatures {
            dataBinding = false
            mlModelBinding = false
            prefabPublishing = false
          }

          buildTypes.configureEach {
            // If you enable these with Kover in the same module, you'll get jacoco being loaded
            // twice.
            // See https://github.com/Kotlin/kotlinx-kover/issues/739
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
          }
        }
      }
}

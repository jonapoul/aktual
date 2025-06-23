import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.koverExcludes
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

dependencies {
  api(libs.androidx.compose.runtime)
  api(libs.haze.core)
  api(libs.kotlinx.immutable)
  api(libs.lazycolumn.scrollbar)
  api(projects.modules.core.model)
  implementation(libs.alakazam.android.compose)
  implementation(libs.androidx.compose.animation.core)
  implementation(libs.androidx.compose.foundation.core)
  implementation(libs.androidx.compose.foundation.layout)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.text)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.ui.toolingPreview)
  implementation(libs.androidx.compose.ui.unit)
  implementation(libs.androidx.coreKtx)
  implementation(libs.kotlinx.coroutines)
  implementation(projects.modules.core.android)
  implementation(projects.modules.l10n)
}

koverExcludes {
  packages(
    "actual.core.icons",
    "actual.core.ui",
  )
}

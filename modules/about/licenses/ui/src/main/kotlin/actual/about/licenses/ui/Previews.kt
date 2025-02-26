package actual.about.licenses.ui

import actual.about.licenses.data.LibraryModel
import actual.about.licenses.data.LicenseModel

internal val AlakazamAndroidCore = LibraryModel(
  project = "Alakazam Android Core",
  description = "A set of useful functions and extensions for Android development.",
  version = "4.5.0",
  developers = listOf("Jon Poulton"),
  url = "https://github.com/jonapoul/alakazam",
  year = 2021,
  licenses = listOf(LicenseModel.Apache2),
  dependency = "dev.jonpoulton.alakazam:android-core",
)

internal val ComposeMaterialRipple = LibraryModel(
  project = "Compose Material Ripple",
  description = "Material ripple used to build interactive components",
  version = "1.6.4",
  developers = listOf("AOSP"),
  url = "https://developer.android.com/jetpack/androidx/releases/compose-material#1.6.4",
  year = 2020,
  licenses = listOf(LicenseModel.Apache2),
  dependency = "androidx.compose.material:material-ripple-android",
)

internal val FragmentKtx = LibraryModel(
  project = "Fragment Kotlin Extensions",
  description = "Kotlin extensions for 'fragment' artifact",
  version = "1.6.2",
  developers = listOf("AOSP"),
  url = "https://developer.android.com/jetpack/androidx/releases/fragment#1.6.2",
  year = 2018,
  licenses = listOf(LicenseModel.Apache2),
  dependency = "androidx.fragment:fragment-ktx",
)

internal val VoyagerScreenModel = LibraryModel(
  project = "VoyagerScreenModel",
  description = "A pragmatic navigation library for Jetpack Compose",
  version = "1.1.0-alpha04",
  developers = listOf("Adriel Cafe"),
  url = "https://github.com/adrielcafe/voyager",
  year = 2021,
  licenses = listOf(LicenseModel.MIT),
  dependency = "cafe.adriel.voyager:voyager-screenmodel-android",
)

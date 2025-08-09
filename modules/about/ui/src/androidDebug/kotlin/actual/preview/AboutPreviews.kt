package actual.preview

import actual.about.data.Apache2
import actual.about.data.ArtifactDetail
import actual.about.data.ArtifactScm
import actual.about.data.UnknownLicense
import actual.about.vm.BuildState
import actual.core.model.ActualVersions

internal val PreviewBuildState = BuildState(
  versions = ActualVersions(app = "1.2.3", server = "2.3.4"),
  buildDate = "12:34 GMT, 1st Feb 2024",
  year = 2024,
)

internal val AlakazamAndroidCore = ArtifactDetail(
  groupId = "dev.jonpoulton.alakazam",
  artifactId = "android-core",
  version = "6.0.0",
  name = "Alakazam Android Core",
  spdxLicenses = setOf(Apache2),
  scm = ArtifactScm(url = "https://github.com/jonapoul/alakazam"),
)

internal val ComposeMaterialRipple = ArtifactDetail(
  groupId = "androidx.compose.material",
  artifactId = "material-ripple",
  version = "1.7.8",
  name = "Compose Material Ripple",
  spdxLicenses = setOf(Apache2),
  scm = ArtifactScm(url = "https://cs.android.com/androidx/platform/frameworks/support"),
)

internal val FragmentKtx = ArtifactDetail(
  groupId = "androidx.fragment",
  artifactId = "fragment-ktx",
  version = "1.8.6",
  name = "Fragment Kotlin Extensions",
  spdxLicenses = setOf(Apache2),
  scm = ArtifactScm(url = "https://cs.android.com/androidx/platform/frameworks/support"),
)

internal val Slf4jApi = ArtifactDetail(
  groupId = "org.slf4j",
  artifactId = "slf4j-api",
  version = "2.0.17",
  name = "SLF4J API Module",
  unknownLicenses = setOf(UnknownLicense(name = "MIT", url = "https://opensource.org/license/mit")),
  scm = ArtifactScm(url = "https://github.com/qos-ch/slf4j/slf4j-parent"),
)

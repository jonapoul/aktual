package aktual.about.ui.licenses

import aktual.about.data.Apache2
import aktual.about.data.ArtifactDetail
import aktual.about.data.ArtifactScm
import aktual.about.data.UnknownLicense

internal val AlakazamAndroidCore =
    ArtifactDetail(
        groupId = "dev.jonpoulton.alakazam",
        artifactId = "android-core",
        version = "6.0.0",
        name = "Alakazam Android Core",
        spdxLicenses = setOf(Apache2),
        scm = ArtifactScm(url = "https://github.com/jonapoul/alakazam"),
    )

internal val ComposeMaterialRipple =
    ArtifactDetail(
        groupId = "androidx.compose.material",
        artifactId = "material-ripple",
        version = "1.7.8",
        name = "Compose Material Ripple",
        spdxLicenses = setOf(Apache2),
        scm = ArtifactScm(url = "https://cs.android.com/androidx/platform/frameworks/support"),
    )

internal val FragmentKtx =
    ArtifactDetail(
        groupId = "androidx.fragment",
        artifactId = "fragment-ktx",
        version = "1.8.6",
        name = "Fragment Kotlin Extensions",
        spdxLicenses = setOf(Apache2),
        scm = ArtifactScm(url = "https://cs.android.com/androidx/platform/frameworks/support"),
    )

internal val Slf4jApi =
    ArtifactDetail(
        groupId = "org.slf4j",
        artifactId = "slf4j-api",
        version = "2.0.17",
        name = "SLF4J API Module",
        unknownLicenses =
            setOf(UnknownLicense(name = "MIT", url = "https://opensource.org/license/mit")),
        scm = ArtifactScm(url = "https://github.com/qos-ch/slf4j/slf4j-parent"),
    )

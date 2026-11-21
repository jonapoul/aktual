package aktual.about.data

import aktual.about.model.Apache2
import aktual.about.model.ArtifactDetail
import aktual.about.model.ArtifactScm
import aktual.about.model.SpdxLicense

private val Mit =
  SpdxLicense(identifier = "MIT", name = "MIT License", url = "https://opensource.org/license/mit")

private val Gpl3 =
  SpdxLicense(
    identifier = "GPL-3.0-only",
    name = "GNU General Public License v3.0 only",
    url = "https://www.gnu.org/licenses/gpl-3.0.html",
  )

private val OpenFont =
  SpdxLicense(
    identifier = "OFL-1.1",
    name = "SIL Open Font License 1.1",
    url = "https://openfontlicense.org/open-font-license-official-text",
  )

internal val ActualBudget =
  ArtifactDetail(
    groupId = "actualbudget",
    artifactId = "actual",
    version = "",
    name = "Actual Budget",
    spdxLicenses = setOf(Mit),
    scm = ArtifactScm(url = "https://github.com/actualbudget/actual"),
  )

internal val Flagpack =
  ArtifactDetail(
    groupId = "yummygum",
    artifactId = "flagpack-core",
    version = "2.1.0",
    name = "Flagpack",
    spdxLicenses = setOf(Mit),
    scm = ArtifactScm(url = "https://github.com/Yummygum/flagpack-core"),
  )

internal val InterFont =
  ArtifactDetail(
    groupId = "rsms",
    artifactId = "inter",
    version = "3.19",
    name = "Inter",
    spdxLicenses = setOf(OpenFont),
    scm = ArtifactScm(url = "https://github.com/rsms/inter"),
  )

internal val MaterialDesignIcons =
  ArtifactDetail(
    groupId = "google",
    artifactId = "material-design-icons",
    version = "",
    name = "Material Design Icons",
    spdxLicenses = setOf(Apache2),
    scm = ArtifactScm(url = "https://github.com/google/material-design-icons"),
  )

internal val ObtainiumBadge =
  ArtifactDetail(
    groupId = "imranr98",
    artifactId = "obtainium",
    version = "",
    name = "Obtainium badge",
    spdxLicenses = setOf(Gpl3),
    scm = ArtifactScm(url = "https://github.com/ImranR98/Obtainium"),
  )

internal val RedactedScriptFont =
  ArtifactDetail(
    groupId = "christiannaths",
    artifactId = "redacted-font",
    version = "1.001",
    name = "Redacted Script",
    spdxLicenses = setOf(OpenFont),
    scm = ArtifactScm(url = "https://github.com/christiannaths/redacted-font"),
  )

// Copied or ported into the app rather than pulled in as dependencies, so licensee can't see them
internal val NonLibraryArtifacts =
  listOf(
    ActualBudget,
    Flagpack,
    InterFont,
    MaterialDesignIcons,
    ObtainiumBadge,
    RedactedScriptFont,
  )

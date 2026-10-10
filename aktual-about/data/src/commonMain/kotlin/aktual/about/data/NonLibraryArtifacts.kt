package aktual.about.data

private val Mit =
  SpdxLicense(identifier = "MIT", name = "MIT License", url = "https://opensource.org/license/mit")

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
  listOf(ActualBudget, InterFont, MaterialDesignIcons, RedactedScriptFont)

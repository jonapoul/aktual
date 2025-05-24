package actual.about.info.ui

import actual.about.info.vm.BuildState
import actual.core.model.ActualVersions

internal val PreviewBuildState = BuildState(
  versions = ActualVersions(app = "1.2.3", server = "2.3.4"),
  buildDate = "12:34 GMT, 1st Feb 2024",
  sourceCodeRepo = "jonapoul/actual-android",
  year = 2024,
)

package actual.about.ui.info

import actual.about.vm.BuildState
import actual.core.model.ActualVersions

internal val PreviewBuildState = BuildState(
  versions = ActualVersions(app = "1.2.3", server = "2.3.4"),
  buildDate = "12:34 GMT, 1st Feb 2024",
  year = 2024,
)

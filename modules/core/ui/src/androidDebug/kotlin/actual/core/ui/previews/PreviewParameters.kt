package actual.core.ui.previews

import actual.core.model.ColorSchemeType
import actual.core.ui.PreviewParameters

internal class BooleanParameters : PreviewParameters<Boolean>(true, false)

internal class ColorSchemeParameters : PreviewParameters<ColorSchemeType>(ColorSchemeType.entries)

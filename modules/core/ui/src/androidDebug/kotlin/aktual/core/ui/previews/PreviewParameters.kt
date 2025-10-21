/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui.previews

import aktual.core.model.ColorSchemeType
import aktual.core.ui.PreviewParameters

internal class BooleanParameters : PreviewParameters<Boolean>(true, false)

internal class ColorSchemeParameters : PreviewParameters<ColorSchemeType>(ColorSchemeType.entries)

@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group

val AktualIcons.Git: ImageVector by lazy {
  aktualIcon(name = "Git", size = 78f) {
    group(translationX = 10f, translationY = 10f, rotate = -45f, pivotX = 29f, pivotY = 29f) {
      aktualPath {
        moveTo(5f, 58f)
        curveToRelative(-2.76142f, 0f, -5f, -2.23858f, -5f, -5f)
        verticalLineToRelative(-48f)
        curveToRelative(0f, -2.76142f, 2.23858f, -5f, 5f, -5f)
        horizontalLineToRelative(33f)
        verticalLineToRelative(12.54404f)
        curveToRelative(-2.06553f, 0.94801f, -3.5f, 3.03446f, -3.5f, 5.45596f)
        curveToRelative(0f, 0.73514f, 0.13221f, 1.43941f, 0.37415f, 2.09031f)
        lineToRelative(-15.28384f, 15.28384f)
        curveToRelative(-0.6509f, -0.24194f, -1.35517f, -0.37415f, -2.09031f, -0.37415f)
        curveToRelative(-3.31371f, 0f, -6f, 2.68629f, -6f, 6f)
        curveToRelative(0f, 3.31371f, 2.68629f, 6f, 6f, 6f)
        curveToRelative(3.31371f, 0f, 6f, -2.68629f, 6f, -6f)
        curveToRelative(0f, -0.73514f, -0.13221f, -1.43941f, -0.37415f, -2.09031f)
        lineToRelative(14.87415f, -14.87415f)
        lineToRelative(0f, 11.50851f)
        curveToRelative(-2.06553f, 0.94801f, -3.5f, 3.03446f, -3.5f, 5.45596f)
        curveToRelative(0f, 3.31371f, 2.68629f, 6f, 6f, 6f)
        curveToRelative(3.31371f, 0f, 6f, -2.68629f, 6f, -6f)
        curveToRelative(0f, -2.42149f, -1.43447f, -4.50795f, -3.5f, -5.45596f)
        lineToRelative(0f, -12.08808f)
        curveToRelative(2.06553f, -0.94801f, 3.5f, -3.03446f, 3.5f, -5.45596f)
        curveToRelative(0f, -2.42149f, -1.43447f, -4.50795f, -3.5f, -5.45596f)
        lineToRelative(0f, -12.54404f)
        horizontalLineToRelative(10f)
        curveToRelative(2.76142f, 0f, 5f, 2.23858f, 5f, 5f)
        verticalLineToRelative(48f)
        curveToRelative(0f, 2.76142f, -2.23858f, 5f, -5f, 5f)
        close()
      }
    }
  }
}

package aktual.core.ui

import aktual.core.theme.Colors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp.Companion.Hairline

fun Modifier.card(colors: Colors, shape: Shape = CardShape): Modifier =
  clip(shape).background(colors.cardBackground, shape).border(Hairline, colors.cardShadow, shape)

fun Modifier.tableRow(colors: Colors, shape: Shape = RowShape): Modifier =
  clip(shape).background(colors.tableBackground, shape).border(Hairline, colors.tableBorder, shape)

fun Modifier.normalButton(colors: Colors, shape: Shape = CardShape): Modifier =
  clip(shape)
    .background(colors.buttonNormalBackground, shape)
    .border(Hairline, colors.buttonNormalBorder, shape)

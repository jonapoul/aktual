package aktual.core.ui

import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

@Composable
fun CardColumn(
  modifier: Modifier = Modifier,
  shape: Shape = CardShape,
  contentPadding: PaddingValues = Dimens.VeryLarge.padding,
  verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  onClick: (() -> Unit)? = null,
  onClickLabel: String? = null,
  content: @Composable ColumnScope.() -> Unit,
) =
  Column(
    modifier = modifier.cardContainer(colors, shape, contentPadding, onClick, onClickLabel),
    verticalArrangement = verticalArrangement,
    horizontalAlignment = horizontalAlignment,
    content = content,
  )

@Composable
fun CardRow(
  modifier: Modifier = Modifier,
  shape: Shape = CardShape,
  contentPadding: PaddingValues = Dimens.VeryLarge.padding,
  horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
  verticalAlignment: Alignment.Vertical = Alignment.Top,
  onClick: (() -> Unit)? = null,
  onClickLabel: String? = null,
  content: @Composable RowScope.() -> Unit,
) =
  Row(
    modifier = modifier.cardContainer(colors, shape, contentPadding, onClick, onClickLabel),
    horizontalArrangement = horizontalArrangement,
    verticalAlignment = verticalAlignment,
    content = content,
  )

private fun Modifier.cardContainer(
  colors: Colors,
  shape: Shape,
  contentPadding: PaddingValues,
  onClick: (() -> Unit)?,
  onClickLabel: String?,
): Modifier =
  card(colors, shape)
    .then(
      if (onClick == null) {
        Modifier
      } else {
        Modifier.clickable(onClickLabel = onClickLabel, onClick = onClick)
      },
    )
    .padding(contentPadding)

@Preview
@Composable
private fun PreviewCard(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(
      modifier = Modifier.padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(Dimens.Large),
    ) {
      CardColumn(modifier = Modifier.fillMaxWidth()) { Text("Default") }
      CardColumn(modifier = Modifier.fillMaxWidth(), shape = RounderCardShape, onClick = {}) {
        Text("Rounder and clickable")
      }
      CardColumn(modifier = Modifier.fillMaxWidth(), contentPadding = Zero) {
        Text("No padding")
      }
      CardRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        onClick = {},
      ) {
        Text("Row")
        Text("Clickable")
      }
    }
  }

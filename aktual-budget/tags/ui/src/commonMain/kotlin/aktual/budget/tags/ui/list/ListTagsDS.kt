package aktual.budget.tags.ui.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

internal object ListTagsDS {
  val listContentPadding = PaddingValues(all = 16.dp)
  val listItemSpacing = 8.dp
  val itemPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
  val itemHorizontalSpacing = 8.dp
  val itemContentSpacing = 6.dp
  val chipShape = RoundedCornerShape(size = 16.dp)
  val chipPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)
  const val HIDDEN_ALPHA = 0.5f
}

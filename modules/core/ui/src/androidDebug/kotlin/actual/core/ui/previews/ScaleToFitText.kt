/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.core.ui.previews

import actual.core.ui.ScaleToFitText
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun ScaleToFitTextPreview() {
  Column {
    ScaleToFitText(
      modifier = Modifier
        .height(100.dp)
        .width(500.dp)
        .background(Color.Gray)
        .border(width = 1.dp, color = Color.Blue),
      text = "Hello world 100x500",
    )

    ScaleToFitText(
      modifier = Modifier
        .height(30.dp)
        .width(500.dp)
        .background(Color.Gray)
        .border(width = 1.dp, color = Color.Red),
      text = "Hello world 30x500",
    )

    Row {
      ScaleToFitText(
        modifier = Modifier
          .height(100.dp)
          .width(100.dp)
          .background(Color.Gray)
          .border(width = 1.dp, color = Color.Green),
        text = "Hello world 100x100",
      )
      ScaleToFitText(
        modifier = Modifier
          .size(12.dp)
          .background(Color.Gray)
          .border(width = 1.dp, color = Color.Yellow),
        text = "15.dp",
      )
      ScaleToFitText(
        modifier = Modifier
          .height(100.dp)
          .weight(1f)
          .background(Color.Gray)
          .border(width = 1.dp, color = Color.Cyan),
        text = "Hello world weighted but loads more text here and here's some more",
      )
    }
  }
}

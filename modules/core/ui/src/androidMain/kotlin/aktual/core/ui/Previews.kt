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
@file:Suppress("PreviewAnnotationNaming", "ModifierNotUsedAtRoot")

package aktual.core.ui

import aktual.core.model.ColorSchemeType
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PreviewContextConfigurationEffect() {
  CompositionLocalProvider(LocalInspectionMode provides true) {
    org.jetbrains.compose.resources
      .PreviewContextConfigurationEffect()
  }
}

abstract class PreviewParameters<T>(private val collection: Collection<T>) : PreviewParameterProvider<T> {
  override val values: Sequence<T> get() = collection.asSequence()

  constructor(vararg values: T) : this(values.toList())
}

@Composable
fun PreviewThemedColumn(
  modifier: Modifier = Modifier,
  isPrivacyEnabled: Boolean = false,
  maxHeight: Dp = Dp.Unspecified,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  LazyColumn {
    items(
      SchemeTypes,
      key = { it.ordinal },
    ) { schemeType ->
      Box(modifier = modifier.heightIn(max = maxHeight)) {
        PreviewWithColorScheme(
          schemeType = schemeType,
          isPrivacyEnabled = isPrivacyEnabled,
          content = content,
        )
      }
    }
  }
}

@Composable
fun PreviewThemedRow(
  modifier: Modifier = Modifier,
  privacyFilter: Boolean = false,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  LazyRow {
    items(
      SchemeTypes,
      key = { it.ordinal },
    ) { schemeType ->
      Box(modifier = modifier) {
        PreviewWithColorScheme(
          schemeType = schemeType,
          isPrivacyEnabled = privacyFilter,
          content = content,
        )
      }
    }
  }
}

@Composable
fun PreviewThemedScreen(
  modifier: Modifier = Modifier,
  isPrivacyEnabled: Boolean = false,
  content: @Composable (ColorSchemeType) -> Unit,
) {
  LazyRow {
    items(
      SchemeTypes,
      key = { it.ordinal },
    ) { schemeType ->
      PreviewWithColorScheme(
        modifier = modifier
          .width(MY_PHONE_WIDTH_DP.dp)
          .height(MY_PHONE_HEIGHT_DP.dp),
        schemeType = schemeType,
        isPrivacyEnabled = isPrivacyEnabled,
        content = { content(schemeType) },
      )
    }
  }
}

@Composable
fun PreviewWithColorScheme(
  schemeType: ColorSchemeType,
  modifier: Modifier = Modifier,
  isPrivacyEnabled: Boolean = false,
  content: @Composable (ColorSchemeType) -> Unit,
) = WithCompositionLocals(
  isPrivacyEnabled = isPrivacyEnabled,
) {
  AktualTheme(schemeType) {
    Surface(modifier = modifier) {
      content(schemeType)
    }
  }
}

@Preview(
  name = "Screen",
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED,
  widthDp = 3 * MY_PHONE_WIDTH_DP,
  heightDp = MY_PHONE_HEIGHT_DP,
)
annotation class TripleScreenPreview

@Preview(
  name = "Screen",
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED,
  widthDp = MY_PHONE_WIDTH_DP,
  heightDp = MY_PHONE_HEIGHT_DP,
)
annotation class SingleScreenPreview

private const val MY_PHONE_DPI = 400
const val MY_PHONE_WIDTH_DP = 1080 / (MY_PHONE_DPI / 160)
const val MY_PHONE_HEIGHT_DP = 2280 / (MY_PHONE_DPI / 160)

val SchemeTypes = persistentListOf(
  ColorSchemeType.Light,
  ColorSchemeType.Dark,
  ColorSchemeType.Midnight,
)

package actual.licenses.ui

import actual.core.res.CoreDimens
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.NormalIconButton
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.licenses.data.LibraryModel
import actual.licenses.res.LicensesStrings
import alakazam.android.ui.compose.HorizontalSpacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
internal fun LibraryItem(
  library: LibraryModel,
  onLaunchUrl: (url: String) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier
      .shadow(CoreDimens.medium)
      .padding(CoreDimens.small)
      .background(theme.cardBackground, CardShape)
      .padding(CoreDimens.huge),
    verticalAlignment = Alignment.Top,
  ) {
    Column(
      modifier = Modifier.weight(1f),
    ) {
      Text(
        text = library.project,
        fontWeight = FontWeight.W700,
        color = theme.pageTextPositive,
        fontSize = 15.sp,
      )

      LibraryTableRow(title = LicensesStrings.itemAuthors, value = library.developers.joinToString())
      LibraryTableRow(title = LicensesStrings.itemArtifact, value = library.dependency)
      LibraryTableRow(title = LicensesStrings.itemVersion, value = library.version)
      LibraryTableRow(title = LicensesStrings.itemYear, value = library.year?.toString())
      LibraryTableRow(title = LicensesStrings.itemLicense, value = library.licenses.firstOrNull()?.license)
      LibraryTableRow(title = LicensesStrings.itemDescription, value = library.description)
    }

    val url = library.url
    if (url != null) {
      NormalIconButton(
        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
        contentDescription = LicensesStrings.itemLaunch,
        onClick = { onLaunchUrl.invoke(url) },
      )
    }
  }
}

@Stable
@Composable
private fun LibraryTableRow(
  title: String,
  value: String?,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  if (value.isNullOrEmpty()) {
    // Nothing to show
    return
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      modifier = Modifier
        .weight(TITLE_WEIGHT)
        .wrapContentHeight(),
      text = title,
      textAlign = TextAlign.Start,
      color = theme.pageTextSubdued,
      lineHeight = LineHeight,
      fontSize = TextSize,
      fontWeight = FontWeight.Bold,
    )

    HorizontalSpacer(CoreDimens.medium)

    Text(
      modifier = Modifier
        .weight(VALUE_WEIGHT)
        .wrapContentHeight(),
      text = value,
      textAlign = TextAlign.Start,
      color = theme.pageText,
      lineHeight = LineHeight,
      fontSize = TextSize,
    )
  }
}

private const val TITLE_WEIGHT = 1f
private const val VALUE_WEIGHT = 3f

private val LineHeight = 15.sp
private val TextSize = 12.sp

@Preview
@Composable
private fun PreviewItem() = PreviewColumn {
  LibraryItem(
    library = AlakazamAndroidCore,
    onLaunchUrl = {},
  )
}

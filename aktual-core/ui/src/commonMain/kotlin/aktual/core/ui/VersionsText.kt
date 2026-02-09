package aktual.core.ui

import aktual.core.model.AktualVersions
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VersionsText(
    versions: AktualVersions,
    modifier: Modifier = Modifier,
    padding: Dp = 15.dp,
) =
    Text(
        modifier = modifier.padding(padding),
        text = versions.toString(),
        style = AktualTypography.labelMedium,
    )

@Preview
@Composable
private fun PreviewVersionsText(
    @PreviewParameter(VersionsTextProvider::class) params: ThemedParams<AktualVersions>,
) = PreviewWithColorScheme(params.type) { VersionsText(params.data) }

private class VersionsTextProvider :
    ThemedParameterProvider<AktualVersions>(
        AktualVersions(app = "1.2.3", server = null),
        AktualVersions(app = "1.2.3", server = "2.3.4"),
    )

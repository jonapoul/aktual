package aktual.core.ui

import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
expect fun SetStatusBarColors(
    theme: Theme = LocalTheme.current,
    statusBarColor: Color = theme.mobileHeaderBackground,
    navigationBarColor: Color = theme.pageBackground,
)

// space to block out the bottom navigation bar, so we don't need to adjust layouts to account for
// it
@Composable
fun BottomNavBarSpacing(modifier: Modifier = Modifier) =
    VerticalSpacer(
        modifier = modifier,
        height = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
    )

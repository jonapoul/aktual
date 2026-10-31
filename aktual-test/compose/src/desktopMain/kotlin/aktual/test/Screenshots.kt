package aktual.test

import aktual.core.theme.Colors
import aktual.core.theme.DarkColors
import aktual.core.ui.MY_PHONE_HEIGHT_DP
import aktual.core.ui.MY_PHONE_WIDTH_DP
import aktual.core.ui.PreviewWithColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import java.io.File
import org.jetbrains.skia.Image

/**
 * Renders [content] headlessly and saves it as `build/screenshots/<name>.png` in the module under
 * test. Used to grab images for PR descriptions, not for screenshot testing.
 *
 * With [fitContent], the image is cropped to the content's own size rather than the whole window.
 * [interact] runs before capture, e.g. to click something that opens a sheet.
 */
@OptIn(ExperimentalTestApi::class)
fun screenshot(
  name: String,
  colors: Colors = DarkColors,
  widthDp: Int = MY_PHONE_WIDTH_DP,
  heightDp: Int = MY_PHONE_HEIGHT_DP,
  density: Float = 2f,
  fitContent: Boolean = false,
  interact: ComposeUiTest.() -> Unit = {},
  content: @Composable () -> Unit,
) {
  runSkikoComposeUiTest(
    size = Size(widthDp * density, heightDp * density),
    density = Density(density),
  ) {
    setContent { PreviewWithColors(colors, Modifier.testTag(CONTENT_TAG), content = content) }
    waitForIdle()
    interact()
    waitForIdle()
    val node = if (fitContent) onNodeWithTag(CONTENT_TAG) else onRoot()
    val bitmap = node.captureToImage().asSkiaBitmap()
    val bytes = Image.makeFromBitmap(bitmap).encodeToData(PNG)?.bytes
    val file = File("build/screenshots/$name.png").absoluteFile
    file.parentFile.mkdirs()
    file.writeBytes(requireNotNull(bytes) { "Failed encoding $name" })
  }
}

private const val CONTENT_TAG = "screenshot-content"

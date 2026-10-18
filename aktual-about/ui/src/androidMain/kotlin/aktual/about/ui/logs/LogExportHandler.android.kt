package aktual.about.ui.logs

import aktual.about.vm.logs.LogExport
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.logcat

@Composable
internal actual fun rememberLogExportHandler(): LogExportHandler {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val handler = remember(context, scope) { AndroidLogExportHandler(context, scope) }
  handler.launcher =
    rememberLauncherForActivityResult(CreateDocument(MIME_TYPE), handler::onSaveResult)
  return handler
}

@Suppress("InjectDispatcher")
private class AndroidLogExportHandler(
  private val context: Context,
  private val scope: CoroutineScope,
) : LogExportHandler {
  var launcher: ManagedActivityResultLauncher<String, Uri?>? = null
  private var pendingText: String? = null

  override val canShare = true

  override fun share(export: LogExport) {
    scope.launch {
      val uri =
        withContext(Dispatchers.IO) {
          val dir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
          val file = File(dir, export.fileName).apply { writeText(export.text) }
          FileProvider.getUriForFile(context, "${context.packageName}.logs", file)
        }

      val intent =
        Intent(Intent.ACTION_SEND)
          .setType(MIME_TYPE)
          .putExtra(Intent.EXTRA_STREAM, uri)
          .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      context.startActivity(Intent.createChooser(intent, export.fileName))
    }
  }

  override fun save(export: LogExport) {
    pendingText = export.text
    launcher?.launch(export.fileName)
  }

  fun onSaveResult(uri: Uri?) {
    val text = pendingText ?: return
    pendingText = null
    if (uri == null) return
    scope.launch(Dispatchers.IO) {
      context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
      logcat.i { "Saved logs to $uri" }
    }
  }
}

private const val MIME_TYPE = "text/plain"
private const val EXPORT_DIR = "logs"

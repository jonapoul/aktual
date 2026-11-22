package aktual.core.ui

import aktual.log
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
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberFileExportHandler(mimeType: String, saveTitle: String): FileExportHandler {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val handler =
    remember(context, scope, mimeType) { AndroidFileExportHandler(context, scope, mimeType) }
  handler.launcher =
    rememberLauncherForActivityResult(CreateDocument(mimeType), handler::onSaveResult)
  return handler
}

@Suppress("InjectDispatcher")
private class AndroidFileExportHandler(
  private val context: Context,
  private val scope: CoroutineScope,
  private val mimeType: String,
) : FileExportHandler {
  var launcher: ManagedActivityResultLauncher<String, Uri?>? = null
  private var pending: ExportFile? = null

  override val canShare = true

  override fun share(file: ExportFile) {
    scope.launch {
      val uri =
        withContext(Dispatchers.IO) {
          // Exports can hold budgets or server details, so don't leave old ones lying around
          val dir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
          dir.listFiles()?.forEach(File::delete)
          val target = File(dir, file.fileName)
          target.outputStream().use(file.write)
          FileProvider.getUriForFile(context, "${context.packageName}.exports", target)
        }

      val intent =
        Intent(Intent.ACTION_SEND)
          .setType(mimeType)
          .putExtra(Intent.EXTRA_STREAM, uri)
          .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      context.startActivity(Intent.createChooser(intent, file.fileName))
    }
  }

  override fun save(file: ExportFile) {
    pending = file
    launcher?.launch(file.fileName)
  }

  fun onSaveResult(uri: Uri?) {
    val file = pending ?: return
    pending = null
    if (uri == null) return
    scope.launch(Dispatchers.IO) {
      try {
        context.contentResolver.openOutputStream(uri)?.use(file.write)
        log.i { "Saved ${file.fileName} to $uri" }
      } catch (e: IOException) {
        log.e(e) { "Failed saving ${file.fileName} to $uri" }
      }
    }
  }
}

private const val EXPORT_DIR = "exports"

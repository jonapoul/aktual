package aktual.budget.tags.vm.list

import aktual.budget.db.GetTags
import androidx.compose.ui.graphics.Color

internal fun GetTags.toTagItem(): TagItem? {
  val tag = tag ?: return null
  return TagItem(
    id = id,
    tag = tag,
    color = color?.toColorOrNull(),
    description = description.orEmpty(),
    hidden = hidden == true,
  )
}

// Tag colors are stored as CSS hex strings (e.g. "#aabbcc"), matching upstream's ColorPicker output
@Suppress("MagicNumber")
internal fun String.toColorOrNull(): Color? {
  val hex = removePrefix("#")
  val argb =
    when (hex.length) {
      3 -> "FF" + hex.map { "$it$it" }.joinToString(separator = "")
      6 -> "FF$hex"
      else -> return null
    }
  return argb.toLongOrNull(radix = 16)?.let { Color(it) }
}

package aktual.budget.tags.vm.list

import aktual.budget.db.GetTag
import aktual.budget.db.GetTags
import aktual.budget.model.TagId
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

internal fun GetTags.toTagItem(): TagItem? = toTagItem(id, tag, color, description, hidden)

internal fun GetTag.toTagItem(): TagItem? = toTagItem(id, tag, color, description, hidden)

private fun toTagItem(
  id: TagId,
  tag: String?,
  color: String?,
  description: String?,
  hidden: Boolean?,
): TagItem? {
  val name = tag ?: return null
  return TagItem(
    id = id,
    tag = name,
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

// inverse of [toColorOrNull] — formats as "#rrggbb" for storage, matching upstream
@Suppress("MagicNumber")
internal fun Color.toHex(): String {
  fun Float.toHexByte() = (this * 255f).roundToInt().coerceIn(0, 255).toString(16).padStart(2, '0')
  return "#${red.toHexByte()}${green.toHexByte()}${blue.toHexByte()}"
}

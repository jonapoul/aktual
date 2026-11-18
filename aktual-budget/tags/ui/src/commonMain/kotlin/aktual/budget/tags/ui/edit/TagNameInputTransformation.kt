package aktual.budget.tags.ui.edit

import aktual.budget.tags.domain.sanitiseTagNameInput
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

// Cleans up the tag name as it's typed. Only the changed middle part is replaced, so the cursor
// stays where the user is typing
internal object TagNameInputTransformation : InputTransformation {
  override fun TextFieldBuffer.transformInput() {
    val original = toString()
    val sanitised = sanitiseTagNameInput(original)
    if (sanitised == original) return

    val prefix = original.commonPrefixWith(sanitised).length
    val suffix = original.substring(prefix).commonSuffixWith(sanitised.substring(prefix)).length
    replace(
      prefix,
      original.length - suffix,
      sanitised.substring(prefix, sanitised.length - suffix),
    )
  }
}

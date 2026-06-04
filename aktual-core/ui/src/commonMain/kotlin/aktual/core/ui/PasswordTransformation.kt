package aktual.core.ui

import androidx.compose.foundation.text.input.OutputTransformation

val PasswordTransformation = OutputTransformation { replace(0, length, "•".repeat(length)) }

package aktual.budget.rules.domain

import java.text.Normalizer

internal actual fun stripDiacritics(value: String): String =
  COMBINING_MARKS.replace(Normalizer.normalize(value, Normalizer.Form.NFD), "")

private val COMBINING_MARKS = Regex("\\p{M}")

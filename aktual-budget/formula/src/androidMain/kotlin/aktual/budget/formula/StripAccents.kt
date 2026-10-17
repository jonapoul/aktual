package aktual.budget.formula

import java.text.Normalizer

internal actual fun stripAccents(text: String): String =
  Normalizer.normalize(text, Normalizer.Form.NFD).replace(COMBINING_ACCENTS, "")

private val COMBINING_ACCENTS = Regex("[\\u0300-\\u036f]")

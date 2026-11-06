package aktual.budget.formula

import java.text.Collator

// Primary strength ignores case and accents, like the "base" Intl.Collator HyperFormula uses
internal actual fun compareText(left: String, right: String): Int = COLLATOR.compare(left, right)

private val COLLATOR = Collator.getInstance(US).apply { strength = Collator.PRIMARY }

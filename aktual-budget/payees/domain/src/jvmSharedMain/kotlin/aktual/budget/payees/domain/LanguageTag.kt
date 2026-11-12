package aktual.budget.payees.domain

import java.util.Locale

internal actual fun defaultLanguageTag(): String = Locale.getDefault().toLanguageTag()

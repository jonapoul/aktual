package aktual.budget.model

import fallback.serializer.Fallback
import kotlin.contracts.contract
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Acct], [Description] and [ImportedDescription] are the `transactions` table's column names for
 * [Account], [Payee] and [ImportedPayee]. Upstream stores rule fields under those names, and
 * `RuleJson` in `data:db` maps them back, so loaded rules only use the public ones.
 */
@Serializable
enum class Field {
  @SerialName("acct") Acct,
  @SerialName("account") Account,
  @SerialName("amount") Amount,
  @SerialName("category") Category,
  @SerialName("category_group") CategoryGroup,
  @SerialName("date") Date,
  @SerialName("description") Description,
  @SerialName("notes") Notes,
  @SerialName("payee") Payee,
  @SerialName("payee_name") PayeeName,
  @SerialName("imported_description") ImportedDescription,
  @SerialName("imported_payee") ImportedPayee,
  @SerialName("saved") Saved,
  @SerialName("transfer") Transfer,
  @SerialName("parent") Parent,
  @SerialName("cleared") Cleared,
  @SerialName("reconciled") Reconciled,
  @Fallback Unknown,
}

fun Field?.isIdField(): Boolean {
  contract { returns(true) implies (this@isIdField != null) }
  return this in FIELDS_WITH_IDS
}

// Make sure this stays in sync with [aktual.budget.rules.domain.NameFetcherImpl.name]
private val FIELDS_WITH_IDS =
  setOf<Field>(Acct, Account, Category, CategoryGroup, Payee, Description)

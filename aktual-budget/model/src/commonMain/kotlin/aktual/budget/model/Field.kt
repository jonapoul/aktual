package aktual.budget.model

import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import kotlin.contracts.contract
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(Field.Serializer::class)
enum class Field(override val value: String) : SerializableByString {
  Acct("acct"), // legacy
  Account("account"),
  Amount("amount"),
  Category("category"),
  CategoryGroup("category_group"),
  Date("date"),
  Description("description"),
  Notes("notes"),
  Payee("payee"),
  PayeeName("payee_name"),
  ImportedDescription("imported_description"),
  ImportedPayee("imported_payee"),
  Saved("saved"),
  Transfer("transfer"),
  Parent("parent"),
  Cleared("cleared"),
  Reconciled("reconciled");

  object Serializer : KSerializer<Field> by enumStringSerializer()
}

fun Field?.isIdField(): Boolean {
  contract { returns(true) implies (this@isIdField != null) }
  return this in FIELDS_WITH_IDS
}

// Make sure this stays in sync with [aktual.budget.rules.vm.NameFetcherImpl.name]
private val FIELDS_WITH_IDS =
  setOf<Field>(Acct, Account, Category, CategoryGroup, Payee, Description)

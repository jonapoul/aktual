package aktual.budget.model

import alakazam.kotlin.SerializableByString

enum class DateFormat(override val value: String) : SerializableByString {
  MmDdYyyy(value = "MM/dd/yyyy"),
  DdMmYyyy(value = "dd/MM/yyyy"),
  YyyyMmDd(value = "yyyy-MM-dd"),
  MmDdYyyyDot(value = "MM.dd.yyyy"),
  DdMmYyyyDot(value = "dd.MM.yyyy");

  override fun toString(): String = value

  companion object {
    val Default = YyyyMmDd
  }
}

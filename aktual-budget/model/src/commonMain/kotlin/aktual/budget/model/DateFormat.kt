package aktual.budget.model

enum class DateFormat(val value: String) {
  MmDdYyyy(value = "MM/dd/yyyy"),
  DdMmYyyy(value = "dd/MM/yyyy"),
  YyyyMmDd(value = "yyyy-MM-dd"),
  MmDdYyyyDot(value = "MM.dd.yyyy"),
  DdMmYyyyDot(value = "dd.MM.yyyy");

  override fun toString(): String = value

  companion object {
    val Default = YyyyMmDd

    fun from(value: String?): DateFormat = entries.firstOrNull { it.value == value } ?: Default
  }
}

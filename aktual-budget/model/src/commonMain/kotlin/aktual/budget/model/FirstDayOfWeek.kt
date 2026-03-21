package aktual.budget.model

enum class FirstDayOfWeek(val value: Int) {
  Sunday(value = 0),
  Monday(value = 1),
  Tuesday(value = 2),
  Wednesday(value = 3),
  Thursday(value = 4),
  Friday(value = 5),
  Saturday(value = 6);

  companion object {
    val Default = Monday
  }
}

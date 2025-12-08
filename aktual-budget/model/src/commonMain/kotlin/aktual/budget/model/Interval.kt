package aktual.budget.model

enum class Interval(private val value: String) {
  Daily("Daily"),
  Weekly("Weekly"),
  Monthly("Monthly"),
  Yearly("Yearly"),
  ;

  override fun toString(): String = value
}

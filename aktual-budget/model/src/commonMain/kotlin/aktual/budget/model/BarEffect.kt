package aktual.budget.model

enum class BarEffect {
  None,
  Blur,
  Glass;

  companion object {
    val Default = Blur
  }
}

package aktual.prefs

import aktual.budget.model.BarEffect
import aktual.budget.model.BarGlassStyle

interface SystemUiPreferences {
  val showBottomBar: Preference<Boolean>
  val appBarEffect: Preference<BarEffect>
  val hazeDialogs: Preference<Boolean>
  val barBlurRadius: Preference<Float>
  val barBlurAlpha: Preference<Float>
  val dialogBlurRadius: Preference<Float>
  val dialogBlurAlpha: Preference<Float>
  val glassStyle: Preference<BarGlassStyle>
  val glassCornerRadius: Preference<Float>
}

package aktual.prefs

import aktual.budget.model.BarEffect
import aktual.budget.model.BarGlassStyle

interface SystemUiPreferences {
  val showBottomBar: Preference<Boolean>
  val appBarEffect: Preference<BarEffect>
  val hazeDialogs: Preference<Boolean>
  val hazeRadius: Preference<Float>
  val hazeAlpha: Preference<Float>
  val glassStyle: Preference<BarGlassStyle>
  val glassCornerRadius: Preference<Float>
  val hidePreviewInAppSwitcher: Preference<Boolean>
}

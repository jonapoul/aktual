package aktual.prefs

import aktual.budget.model.BarEffect

interface SystemUiPreferences {
  val showBottomBar: Preference<Boolean>
  val appBarEffect: Preference<BarEffect>
  val hazeDialogs: Preference<Boolean>
  val hazeRadius: Preference<Float>
  val hazeAlpha: Preference<Float>
  val hidePreviewInAppSwitcher: Preference<Boolean>
}

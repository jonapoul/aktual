package aktual.core.model

import alakazam.kotlin.StateHolder

// Whether the app has a visible window. Android sets this from the activity lifecycle, desktop
// leaves it true
class ForegroundStateHolder : StateHolder<Boolean>(initialState = true)

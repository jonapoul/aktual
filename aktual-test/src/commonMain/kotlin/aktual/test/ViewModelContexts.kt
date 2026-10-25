package aktual.test

import aktual.di.ViewModelContext
import alakazam.test.standardDispatcher
import alakazam.test.unconfinedDispatcher
import kotlinx.coroutines.test.TestScope

val TestScope.viewModelContext: ViewModelContext
  get() = ViewModelContext { standardDispatcher }

// Runs eagerly like Main.immediate does in the app
val TestScope.unconfinedViewModelContext: ViewModelContext
  get() = ViewModelContext { unconfinedDispatcher }

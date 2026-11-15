package aktual.budget.rules.domain

import kotlinx.coroutines.flow.Flow

// The global learn-categories synced pref, read as upstream's String(value ?? 'true') === 'true'
interface CategoryLearningPref {
  fun observe(): Flow<Boolean>

  suspend fun isEnabled(): Boolean

  suspend fun set(enabled: Boolean)
}

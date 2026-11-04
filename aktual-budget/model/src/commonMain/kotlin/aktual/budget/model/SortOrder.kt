package aktual.budget.model

import kotlin.math.floor

// SORT_INCREMENT in packages/loot-core/src/server/db/sort.ts, the gap between consecutive rows'
// sort_order
const val SORT_INCREMENT = 16384L

// The sort_order that places a new row after [max]. Messages only carry whole numbers, so this
// rounds down first, which still puts it after a fractional order
fun sortOrderAfter(max: Double?): Long = (max?.let(::floor)?.toLong() ?: 0L) + SORT_INCREMENT

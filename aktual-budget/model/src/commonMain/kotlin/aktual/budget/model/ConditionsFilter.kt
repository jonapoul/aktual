package aktual.budget.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/** The transactions matching every one of [sets], and tagged with exactly [tags] when it's set */
@Immutable
@Serializable
data class ConditionsFilter(val sets: List<ConditionSet>, val tags: ExactTags? = null)

@Immutable
@Serializable
data class ConditionSet(val conditions: List<Condition>, val op: ConditionOp = And)

/**
 * Notes holding exactly [names] of the tags in [scope], and none of the others. Matched by exact
 * name, as a report split by tag buckets them
 */
@Immutable @Serializable data class ExactTags(val names: List<String>, val scope: List<String>)

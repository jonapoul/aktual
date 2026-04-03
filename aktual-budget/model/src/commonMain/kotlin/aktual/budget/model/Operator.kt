package aktual.budget.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Immutable
@Serializable(OperatorSerializer::class)
sealed interface Operator {
  data object Contains :
    AccountOperator,
    CategoryOperator,
    CategoryGroupOperator,
    NotesOperator,
    PayeeOperator,
    ImportedPayeeOperator

  data object DoesNotContain :
    AccountOperator,
    CategoryOperator,
    CategoryGroupOperator,
    NotesOperator,
    PayeeOperator,
    ImportedPayeeOperator

  data object GreaterThan : AmountOperator, DateOperator

  data object GreaterThanOrEquals : AmountOperator, DateOperator

  data object HasTags : NotesOperator

  data object Is :
    AccountOperator,
    CategoryOperator,
    CategoryGroupOperator,
    AmountOperator,
    DateOperator,
    NotesOperator,
    PayeeOperator,
    ImportedPayeeOperator,
    SavedOperator,
    ClearedOperator,
    ReconciledOperator,
    TransferOperator

  data object IsApprox : AmountOperator, DateOperator

  data object IsBetween : AmountOperator, DateOperator

  data object IsNot :
    AccountOperator,
    CategoryOperator,
    CategoryGroupOperator,
    NotesOperator,
    PayeeOperator,
    ImportedPayeeOperator

  data object LessThan : AmountOperator, DateOperator

  data object LessThanOrEquals : AmountOperator, DateOperator

  data object Matches :
    AccountOperator,
    CategoryOperator,
    CategoryGroupOperator,
    NotesOperator,
    PayeeOperator,
    ImportedPayeeOperator

  data object NotOneOf :
    AccountOperator, CategoryOperator, CategoryGroupOperator, PayeeOperator, ImportedPayeeOperator

  data object OffBudget : AccountOperator

  data object OnBudget : AccountOperator

  data object OneOf :
    AccountOperator, CategoryOperator, CategoryGroupOperator, PayeeOperator, ImportedPayeeOperator

  fun string(): String = OperatorMap.first { it.second == this }.first

  companion object {
    fun parse(string: String): Operator = OperatorMap.single { it.first == string }.second
  }
}

sealed interface AccountOperator : Operator

sealed interface CategoryOperator : Operator

sealed interface CategoryGroupOperator : Operator

sealed interface AmountOperator : Operator

sealed interface DateOperator : Operator

sealed interface NotesOperator : Operator

sealed interface PayeeOperator : Operator

sealed interface ImportedPayeeOperator : Operator

sealed interface SavedOperator : Operator

sealed interface ClearedOperator : Operator

sealed interface ReconciledOperator : Operator

sealed interface TransferOperator : Operator

internal object OperatorSerializer : KSerializer<Operator> {
  override val descriptor = PrimitiveSerialDescriptor("Operator", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Operator = Operator.parse(decoder.decodeString())

  override fun serialize(encoder: Encoder, value: Operator) = encoder.encodeString(value.string())
}

private val OperatorMap: ImmutableSet<Pair<String, Operator>> by lazy {
  persistentSetOf(
    "contains" to Operator.Contains,
    "doesNotContain" to Operator.DoesNotContain,
    "gt" to Operator.GreaterThan,
    "gte" to Operator.GreaterThanOrEquals,
    "hasTags" to Operator.HasTags,
    "is" to Operator.Is,
    "isapprox" to Operator.IsApprox,
    "isbetween" to Operator.IsBetween,
    "isNot" to Operator.IsNot,
    "lt" to Operator.LessThan,
    "lte" to Operator.LessThanOrEquals,
    "matches" to Operator.Matches,
    "notOneOf" to Operator.NotOneOf,
    "offBudget" to Operator.OffBudget,
    "onBudget" to Operator.OnBudget,
    "oneOf" to Operator.OneOf,
  )
}

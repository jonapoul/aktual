# Plan — adapt web RuleEditor's id → name mapping + id pickers to Android `edit/Conditions.kt`

## Context

`aktual-budget/rules/ui/.../edit/Conditions.kt` currently renders a condition's value with:

```kotlin
TextField(
  value = condition.value.toString(), // TODO: use LocalNameFetcher
  onValueChange = {},
  ...
)
```

That's wrong in two ways:

1. `condition.value` is a `JsonElement`. For id-typed fields (payee / category / account / category_group) the raw value is a UUID — unhelpful to the user. The web shows the entity's **name** via `DisplayId`/`PayeeAutocomplete`/etc., resolving id→name via hooks like `usePayee`.
2. The field is read-only (`onValueChange = {}`). You can't change a condition's value today.

The list screen (`list/Strings.kt:rememberConditionText`) already does display-side id→name resolution via the existing `NameFetcher` (`rules/vm/.../NameFetcher.kt`) + `LocalNameFetcher` CompositionLocal. We'll reuse that infrastructure and extend it: add editable pickers and a writable value action, so the edit screen mirrors the web `GenericInput` experience for id fields.

Chosen scope: **display + editable id picker** (user-selected).

## Approach

### 1. Promote `LocalNameFetcher` to module-level

Move `LocalNameFetcher` out of `list/Locals.kt` (where it's `internal` to the `list` package, with a `DummyNameFetcher` fallback) into a shared location so `edit/` can consume it.

- **Delete** `aktual-budget/rules/ui/.../list/Locals.kt`.
- **Create** `aktual-budget/rules/ui/.../Locals.kt` (in the `rules.ui` package). Same contents, just moved up one level — still `internal` to the module.

### 2. Provide `LocalNameFetcher` in `EditRuleScreen`

In `aktual-budget/rules/ui/.../edit/EditRuleScreen.kt`, wrap `EditRuleScaffold(...)` with `CompositionLocalProvider(LocalNameFetcher provides viewModel.nameFetcher) { ... }` — same pattern as `ListRulesScreen.kt:107`. The VM already exposes `nameFetcher` (`EditRuleViewModel.kt:75`).

### 3. Add `SetConditionValue` action + VM handler

- **`edit/EditRuleAction.kt`** — add:
  ```kotlin
  internal data class SetConditionValue(val value: JsonElement, val index: Int) : EditRuleAction
  ```
- **`EditRuleViewModel.kt`** — add:
  ```kotlin
  fun setConditionValue(value: JsonElement, index: Int) =
    updateCondition(index) { c -> c.copy(value = value) }
  ```
- **`EditRuleScreen.kt`** — wire `is SetConditionValue -> viewModel.setConditionValue(action.value, action.index)` into the `when` at the screen's onAction dispatch.

When `setConditionField` changes the field, also clear the value to a type-appropriate default (today it doesn't — follow the web's `onChange` in `RuleEditor.tsx:836`). Extend `setConditionField` to also reset `value = JsonNull` and keep `type` aligned — leave `customName`/options alone.

### 4. Extract `ConditionValueEditor` composable

Create `aktual-budget/rules/ui/.../edit/ConditionValueEditor.kt`:

```kotlin
@Composable
internal fun ConditionValueEditor(
  condition: Condition,
  index: Int,
  isEnabled: Boolean,
  onAction: EditRuleActionHandler,
)
```

Dispatch on `condition.field` + `condition.operator` (mirror `GenericInput.tsx` switch on `type`/`field`):

| Field                                   | Single (`Is`/`IsNot`/etc.)     | Multi (`OneOf`/`NotOneOf`)        |
| --------------------------------------- | ------------------------------ | --------------------------------- |
| `Payee`, `Description` (id)             | `PayeeIdPicker`                | `PayeeIdMultiPicker`              |
| `Category` (id)                         | `CategoryIdPicker`             | `CategoryIdMultiPicker`           |
| `CategoryGroup` (id)                    | `CategoryGroupIdPicker`        | `CategoryGroupIdMultiPicker`      |
| `Account`, `Acct` (id)                  | `AccountIdPicker`              | `AccountIdMultiPicker`            |
| `Notes`, `PayeeName`, `ImportedPayee`   | writable `TextField`           | multi-string (chips) — punt       |
| `Amount`                                | numeric currency input         | between inputs for `IsBetween`    |
| `Date`                                  | date input — **punt**, raw text for now |                            |
| `Cleared`, `Reconciled`, `Transfer`, `Parent`, `OnBudget`/`OffBudget` | no value needed | n/a       |

Each branch reads from `condition.value` (a `JsonElement`) and emits `SetConditionValue(newJsonElement, index)`.

Reuse formatting helpers from `list/Strings.kt` (`formatAmount`, `RecurConfig.string`) where possible — keep them in the `list/` package but mark them `internal` / promote to shared if they're needed from `edit/` too.

### 5. Add id-picker composables

Create a new directory `aktual-budget/rules/ui/.../edit/pickers/` with:

- `PayeeIdPicker.kt`
- `CategoryIdPicker.kt`
- `CategoryGroupIdPicker.kt`
- `AccountIdPicker.kt`

Each picker:

1. Shows the currently-selected entity **name** (resolved via `LocalNameFetcher.current.name(field, id).collectAsStateWithLifecycle(initialValue = "…")`), or a placeholder ("nothing" — like `GenericInput.tsx:160`) when `value` is `JsonNull`.
2. On click, opens a Material3 `ModalBottomSheet` containing a searchable `LazyColumn` of all entities of that type.
3. On selection, emits `SetConditionValue(JsonPrimitive(newId), index)`; on clear, emits `JsonNull`.

**Data source for the picker lists.** The DAOs (`PayeeDao`, `CategoryDao`, `AccountDao`, `CategoryGroupDao`) currently only expose `name`/`names`. The SQL `.sq` files have `getAll` queries (e.g. `Payees.sq:28`). Add flow-returning wrappers to each DAO:

```kotlin
fun observeAll(): Flow<List<Payees>> = queries.asFlow().mapToList(...)
```

Expose these through a new small repository or directly through a new `NameFetcher`-like interface (e.g. `EntityListFetcher`) in `rules/vm/`, provided alongside `nameFetcher` on `EditRuleViewModel`. Don't burden `NameFetcher` with this — keep concerns separate.

Don't build new Payee/Category entity-management UI; the pickers are purely for rule-condition id selection. Existing entity list queries in the `aktual-budget:transactions` feature may already expose flows that we can reuse — check before adding duplicates.

### 6. Wire it up in `Conditions.kt`

Replace lines 137–144 (the `TextField`) with `ConditionValueEditor(condition, index, isEnabled, onAction)`. Remove the `// TODO: use LocalNameFetcher` comment. Keep the rest of `Condition` (field/operator/delete row) untouched.

## Out of scope (future work)

- Full rich date picker (`GenericInput.tsx:367` branch) — recurring schedules, month/year. Today `Date` rules already work in the list view; edit-side can remain a raw string TextField.
- `isbetween` for amounts (needs a two-input editor like web's `BetweenAmountInput`).
- `contains`/`matches`/`oneOf` for ids (the web disables these for id types — `RuleEditor.tsx:151`). Keep that restriction: the operator filter list in `shared.kt:filteredOperators` already omits them for id fields; leave alone.
- Action editing. The value editor work here applies only to conditions. Actions will mirror the same pattern but are not part of this change.
- Schedule `link-schedule` picker.

## Files to change

| Path | Change |
| --- | --- |
| `aktual-budget/rules/ui/src/commonMain/kotlin/aktual/budget/rules/ui/list/Locals.kt` | **Delete** |
| `aktual-budget/rules/ui/src/commonMain/kotlin/aktual/budget/rules/ui/Locals.kt` | **New** — moved from above |
| `aktual-budget/rules/ui/src/commonMain/kotlin/aktual/budget/rules/ui/edit/EditRuleAction.kt` | Add `SetConditionValue` |
| `aktual-budget/rules/vm/src/commonMain/kotlin/aktual/budget/rules/vm/edit/EditRuleViewModel.kt` | Add `setConditionValue`; extend `setConditionField` to reset value |
| `aktual-budget/rules/ui/src/commonMain/kotlin/aktual/budget/rules/ui/edit/EditRuleScreen.kt` | Wrap with `CompositionLocalProvider`; dispatch `SetConditionValue` |
| `aktual-budget/rules/ui/src/commonMain/kotlin/aktual/budget/rules/ui/edit/Conditions.kt` | Replace read-only `TextField` with `ConditionValueEditor` |
| `aktual-budget/rules/ui/src/commonMain/kotlin/aktual/budget/rules/ui/edit/ConditionValueEditor.kt` | **New** — dispatch on field/op |
| `aktual-budget/rules/ui/src/commonMain/kotlin/aktual/budget/rules/ui/edit/pickers/{Payee,Category,CategoryGroup,Account}IdPicker.kt` | **New** |
| `aktual-budget/data/src/commonMain/kotlin/aktual/budget/db/dao/{Payee,Category,CategoryGroup,Account}Dao.kt` | Add `observeAll(): Flow<List<...>>` wrappers |
| `aktual-budget/rules/vm/src/commonMain/kotlin/aktual/budget/rules/vm/edit/EditRuleViewModel.kt` | Expose a list fetcher alongside `nameFetcher` for pickers |

## Reused existing code

- `aktual-budget/rules/vm/.../NameFetcher.kt` — `NameFetcher` interface and `NameFetcherImpl`. Already handles Payee/Account/Category/CategoryGroup/Description. Don't reinvent.
- `aktual-budget/model/.../Field.kt:40` — `Field?.isIdField()` for branching on id fields.
- `aktual-budget/rules/ui/.../list/Strings.kt:rememberConditionText` (lines 96–193) — reference implementation of the name-fetching display pattern via `LocalNameFetcher`. The lookup block at lines 109–127 is the exact shape we need in `ConditionValueEditor` for single/multi id display (handle `JsonPrimitive` / `JsonArray`).
- `aktual-core/ui/.../TextFields.kt` — `ExposedDropDownMenu` could be reused for the picker trigger styling if bottom sheets feel too heavy; pick whichever looks more at home.

## Verification

1. Build the module — fast incremental:
   ```
   ./gradlew :aktual-budget:rules:ui:compileAll :aktual-budget:rules:vm:compileAll --stacktrace --warning-mode=all
   ```
2. Run the module tests (VM-side):
   ```
   ./gradlew :aktual-budget:rules:vm:test --stacktrace --info
   ```
3. Launch Android (`./gradlew :aktual-app:android:installDebug`), open an existing rule with a payee-`Is`-someId condition:
  - Expect: the value reads as the **payee name** (e.g. "Tesco"), not a UUID.
  - Tap the value → bottom sheet of payees appears → pick a different one → on back-navigate the condition shows the newly-selected name.
4. Create a new rule; switch the field from Payee → Notes → Category:
  - Expect: value resets (no stale payee id surfacing as a string); the editor swaps input type appropriately.
5. Save the rule; re-open it and confirm the new id persists.
6. Check `ListRulesScreen` still renders names (regression check — we moved `LocalNameFetcher`, didn't change its contract).
7. Previews in `Conditions.kt` / `EditRuleScreen.kt` still render — supply `LocalNameFetcher` via `CompositionLocalProvider` in the previews, mirroring `DummyNameFetcher` from the old `list/Locals.kt` (keep that dummy around, promoted to the same module-level `Locals.kt` as the CompositionLocal).

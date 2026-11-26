# aktual-core:ui

Shared Compose components and theming.

## AktualTheme

`AktualTheme` is the single entry point for UI tokens. Read tokens from it rather than hardcoding values:

- `AktualTheme.colors` - theme colors (`Colors` from `aktual-theme`). Composable, since it depends on the active theme.
- `AktualTheme.typography` - Material `Typography` built in `AktualTypography.kt` with the Inter font. Composable.
- `AktualTheme.textSizes`, `lineHeights`, `letterSpacings` - `sp` scales in `TextSizes.kt`, `LineHeights.kt` and `LetterSpacings.kt`. Not composable, so they work in top-level vals and default params.

## Text tokens

Never write a raw `N.sp` for a font size, line height or letter spacing. Import the scale statically and use the nearest entry, e.g. `import aktual.core.ui.AktualTheme.textSizes`, then `textSizes.small`. If no entry fits, add one to the scale's file rather than hardcoding. Only sizes computed at runtime (like in `ScaleToFitText`) use `.sp` directly.

Spacing uses `Dimens` (dp), not `AktualTheme`.

## Fonts and separators

- Don't use `FontFamily.Monospace`, except for preformatted text that relies on fixed-width alignment: log messages and stack traces. Elsewhere use the Inter feature helpers in `FontFeatures.kt`: `TextStyle.tabular()` for numbers that should line up (times, sizes, columns), `TextStyle.disambiguated()` for code and IDs (formulas, API keys). Names and prose get plain Inter.
- Never join text with a ` · ` dot separator. Lay the pieces out as separate `Text`s with spacing, or use natural punctuation: commas (`joinToString()`), parentheses (`Queries (3)`), or `; ` when the items can contain commas.

## Cards

Use the `CardColumn` (column) or `CardRow` composable for card containers, passing `contentPadding` and `onClick` rather than chaining `padding`/`clickable` yourself. `Modifier.card` is for cases `CardColumn` can't cover, like applying the card style conditionally or adding `shimmer()` after the padding.

## Setting rows

For a card of settings (label on the left, value or switch on the right), use `SectionLabel` above a card holding `SettingRow`s separated by `SettingDivider`s, all from `SettingRows.kt`. `SettingRow` adds the trailing chevron when it's clickable.

## Loading shimmers

- Build placeholders from `ShimmerLine` (a bar the height of one line of a `TextStyle`), `ShimmerBlock` (icons, switches, charts) and `ShimmerChip` (`Shimmer.kt`). Don't hardcode bar heights in dp. Pass `ShimmerLine` the same font size and weight as the text it replaces.
- Lay the shimmer out like the loaded content: same rows and sections, built from the real composable's padding and size constants, in the same file so they can stay private. Compare the two previews side by side.
- Put `.shimmer()` on the content, not on a card or anything else with a background, or the background fades too. Share one `rememberShimmer(Window)` between the pieces so they shimmer in step.

## Buttons

Use `AktualButton` and `AktualIconButton` (`AktualButtons.kt`), picking a `ButtonKind` (`Primary`, `Normal`, `Bare`, `Tonal`, `Destructive`) and passing `isLoading` rather than building a spinner yourself. Pass `variant = Secondary` (`ButtonVariant`) for the less prominent option next to a filled button, e.g. cancel beside save: transparent body with the kind's colour on the border and text. Only drop to `BasicTextButton` / `BasicIconButton` for colours no kind covers.

In `AktualAlertDialog` button slots (and `AktualDatePickerDialog` confirm/dismiss slots), use `DialogTextButton` instead: tinted text with no background, picking a `DialogButtonKind` (`Normal` for cancel/dismiss, `Primary` for confirm, `Destructive` for delete/discard).

## Edit screens

Screens that edit an entity with unsaved-change tracking use the pieces in `EditScreens.kt`. Put `EditFabs` along the bottom of the screen's `Box` (above the snackbar host if it has one, then `BottomSpacing`): an icon-only discard FAB at the start and save at the end, both toggled with `isSaveVisible` / `isDiscardVisible` and passing `isExpanded = scrollState.isScrollingUp()`. Keep the plain back button in the toolbar, but make it, the discard FAB and `BackHandler` all open `DiscardChangesDialog` (pass the screen's own strings) when there are changes. End the scroll content with `FabClearance` + `BottomSpacing` so the last item can scroll out from under the FABs. `EditRuleScreen` is the reference.

## FABs

Put a list screen's main action (create, add) and its search in floating action buttons, not the top bar. Place them with `CornerFabs` (`CornerFabs.kt`) as the last child of the screen's `Box`: `SearchFab` in `start` (icon only, passing `isFaded = !isScrollingUp`), `AktualFab` in `end` with `isExpanded = listState.isScrollingUp()` so it collapses to a faded icon while scrolling down. Keep the end FAB's label to one word (`Create`, `Add`): the screen title gives the context. Add a `<feature>_fab_create` string if the longer one is used elsewhere. Toggle them with `isVisible` rather than an `if`, so they slide in and out with the shared animation. Hide the create FAB when the empty state has its own create button. End the scroll content with `FabClearance` + `BottomSpacing`. `ListSchedulesScreen` is the reference. Keep search in the top bar when it opens inline there (transactions).

## Progress indicators

Use `AktualProgressIndicator` (`AktualProgressIndicator.kt`), not Material's `CircularProgressIndicator`. Pass `size` rather than `Modifier.size`, so the stroke scales with it. Defaults to `colors.pageText`. For a whole loading screen, use `LoadingScreen(modifier)`, which fills the space and centres the indicator.

## Tooltips

Use `AktualTooltip` (`AktualTooltip.kt`) for an icon button that shows text when tapped, not Material's `TooltipBox`, whose popup lets taps outside it through on desktop. For a "?" beside a label explaining it, use `AktualHelpTooltip(text)`, which takes no more room than its 16dp icon.

## Action handlers

Screens take a single `onAction: OnAction<FooAction>` for their sealed action type. Don't declare a per-screen `fun interface FooActionHandler`.

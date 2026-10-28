# aktual-core:ui

Shared Compose components and theming.

## AktualTheme

`AktualTheme` is the single entry point for UI tokens. Read tokens from it rather than hardcoding values:

- `AktualTheme.colors` - theme colors (`Colors` from `aktual-core:theme`). Composable, since it depends on the active theme.
- `AktualTheme.typography` - Material `Typography` built in `AktualTypography.kt` with the Inter font. Composable.
- `AktualTheme.textSizes`, `lineHeights`, `letterSpacings` - `sp` scales in `TextSizes.kt`, `LineHeights.kt` and `LetterSpacings.kt`. Not composable, so they work in top-level vals and default params.

## Text tokens

Never write a raw `N.sp` for a font size, line height or letter spacing. Import the scale statically and use the nearest entry, e.g. `import aktual.core.ui.AktualTheme.textSizes`, then `textSizes.small`. If no entry fits, add one to the scale's file rather than hardcoding. Only sizes computed at runtime (like in `ScaleToFitText`) use `.sp` directly.

Spacing uses `Dimens` (dp), not `AktualTheme`.

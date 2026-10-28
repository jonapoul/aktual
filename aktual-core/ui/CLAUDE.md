# aktual-core:ui

Shared Compose components and theming.

## AktualTheme

`AktualTheme` is the single entry point for UI tokens. Read tokens from it rather than hardcoding values:

- `AktualTheme.colors` - theme colors (`Colors` from `aktual-core:theme`). Composable, since it depends on the active theme.
- `AktualTheme.typography` - Material `Typography` built in `AktualTypography.kt` with the Inter font. Composable.
- `AktualTheme.textSizes` - font size scale (`TextSizes`). Not composable, so it works in top-level vals and default params.

## Text sizes

Never write a raw `N.sp` for a font size. Use the nearest entry in `TextSizes.kt` (8sp `micro` up to 44sp `hero`), imported statically: `import aktual.core.ui.AktualTheme.textSizes`, then `textSizes.small`. If no entry fits, add one to `TextSizes.kt` rather than hardcoding. Line heights and letter spacing aren't tokenised yet, so `sp` literals for those are fine.

Spacing uses `Dimens` (dp), not `AktualTheme`.

# detekt-rules

Custom detekt rules, loaded as a `detektPlugins` dependency by `ConventionDetekt`.

## Adding a rule

1. Create a class in `aktual.detekt.rules` extending `Rule` (add `RequiresAnalysisApi` if you need type resolution via `analyze {}`).
2. Register it in `AktualRuleSetProvider.instance()`.
3. Add an entry to `config/detekt-aktual.yml` at the repo root (loaded by `ConventionDetekt`), so rules not listed there are never discovered.
4. Add tests. PSI-only rules can use `rule.lint(code)`; `RequiresAnalysisApi` rules need `@KotlinCoreEnvironmentTest` + `KotlinAnalysisApiEngine`.

## How rule discovery works

Detekt reads `config.subConfigKeys()` to find rules. Without a config entry, a rule is silently skipped regardless of `allRules = true`.

## RequiresAnalysisApi

Rules implementing `RequiresAnalysisApi` only run in `AnalysisMode.full`, which requires the Detekt task to have a non-empty `classpath`. The per-compilation tasks (`detektMainJvm`, etc.) set this up automatically; the plain `detekt` task does not.

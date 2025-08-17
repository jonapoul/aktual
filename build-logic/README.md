# Diagram Task Dependencies
```mermaid
flowchart TD
  CalculateProjectTreeTask --> CollateProjectLinksTask
  CheckDotFileTask --> GenerateModulesDotFileTask
  CollateModuleTypesTask --> DumpModuleTypeTask
  CollateProjectLinksTask --> DumpProjectLinksTask
  GenerateModulesDotFileTask --> CalculateProjectTreeTask
  GenerateModulesDotFileTask --> CollateModuleTypesTask
  GeneratePngFileTask --> GenerateModulesDotFileTask
  GeneratePngFileTask --> GenerateLegendDotFileTask
```

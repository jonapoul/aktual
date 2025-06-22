# Diagram Task Dependencies
```mermaid
flowchart TD
  CalculateProjectTreeTask --> CollateProjectLinksTask
  CheckDotFileTask --> GenerateDotFileTask
  CollateModuleTypesTask --> DumpModuleTypeTask
  CollateProjectLinksTask --> DumpProjectLinksTask
  GenerateDotFileTask --> CalculateProjectTreeTask
  GenerateDotFileTask --> CollateModuleTypesTask
  GeneratePngFileTask --> GenerateDotFileTask
```

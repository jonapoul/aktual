# Diagram Task Dependencies

```mermaid
flowchart TD
  CalculateProjectTreeTask -- depends on --> CollateProjectLinksTask
  CollateModuleTypesTask -- depends on --> DumpModuleTypeTask
  CollateProjectLinksTask -- depends on --> DumpProjectLinksTask
  GenerateReadmeTask -- depends on --> CalculateProjectTreeTask
  GenerateReadmeTask -- depends on --> CollateModuleTypesTask
```

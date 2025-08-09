# Diagram Task Dependencies

```mermaid
flowchart TD
  CalculateProjectTreeTask -- depends on --> CollateProjectLinksTask
  CollateModuleTypesTask -- depends on --> DumpModuleTypeTask
  CollateProjectLinksTask -- depends on --> DumpProjectLinksTask
  GenerateMermaidTask -- depends on --> CalculateProjectTreeTask
  GenerateMermaidTask -- depends on --> CollateModuleTypesTask
```

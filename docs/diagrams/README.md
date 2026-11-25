# UML diagrams

| Diagram | Read | Edit |
| --- | --- | --- |
| Overall Use Case Diagram (SAD §4) | [SVG](overall-use-cases.svg) | [PlantUML](overall-use-cases.puml) |
| Play a match: activity | [SVG](gameplay-activity.svg) | [PlantUML](gameplay-activity.puml) |
| Send a friend request: activity | [SVG](friends-activity.svg) | [PlantUML](friends-activity.puml) |
| Gameplay interaction example | [SVG](gameplay-sequence.svg) | [PlantUML](gameplay-sequence.puml) |
| Friend-request interaction example | [SVG](friends-sequence.svg) | [PlantUML](friends-sequence.puml) |
| High-level architecture (SAD §5.1) | [SVG](architecture-overview.svg) | [PlantUML](architecture-overview.puml) |
| Architectural class diagram (SAD §5.2) | [SVG](architecture-class-diagram.svg) | [PlantUML](architecture-class-diagram.puml) |
| Deployment diagram (SAD §7) | [SVG](deployment-diagram.svg) | [PlantUML](deployment-diagram.puml) |
| EER database model (SAD §9) | [SVG](eer-data-model.svg) | [PlantUML](eer-data-model.puml) |

Render with Java and PlantUML 1.2026.4:

```sh
java -Djava.awt.headless=true -jar /path/to/plantuml-1.2026.4.jar -tsvg -failfast2 docs/diagrams/*.puml
```

Run from the repository root and commit the sources and SVGs together. Graphviz is not required.

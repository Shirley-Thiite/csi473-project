 

 Interfaces (Ports)

This folder contains the interface definitions (ports) that the core application logic depends on. Concrete implementations (adapters) live in src/adapters/.

Dependency direction:
 Application services (src/application/) depend on these interfaces.
 Adapters (src/adapters/) implement these interfaces.
 Domain entities (src/domain/) depend on nothing external.

Why this folder exists. The Lab 09 brief requires that core logic depends on stable interfaces rather than concrete external adapters, and that dependency direction is visible in the code. Putting the interfaces here and the adapters in a separate folder makes the inversion explicit and inspectable.

Key guarantee. The CustodyRepository interface exposes only append and findByItemId. There is no update method and no delete method. This makes the append-only guarantee of the Chain of Custody a structural property of the code, not a policy a developer could accidentally bypass. The concrete EventStoreCustodyRepository adapter also uses a database role with INSERT-only privileges, so the guarantee is enforced at both the code level and the database level.

Related artefacts.
 docs/pattern-decisions.md — Section 3.1 explains why the Repository pattern was selected.
 decisions/ADR-002.md — records the dependency inversion decision and the append-only guarantee.
 models/detailed-class-design.puml — shows these interfaces and their relationships.
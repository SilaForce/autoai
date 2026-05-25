# AutoAI Project Rules

You are an expert Android developer specializing in Jetpack Compose, Clean Architecture, MVI, Koin, and Firebase. The rules below are the foundation — they apply to every change. Detailed conventions live in feature-specific skills under `.claude/skills/`, which load automatically when relevant.

## Core Philosophy

- **Clean Architecture:** Strict layering `presentation` → `domain` ← `data`. The **domain** layer is innermost and has **zero dependencies** on any other layer or framework.
- **Firebase-First:** This project relies exclusively on Firebase (Auth, Firestore, Storage, Cloud Functions). **Do not** suggest or introduce Ktor, Retrofit, Room, or SQLDelight unless explicitly requested.
- **Feature-Layered Modularization:** Split by feature first, then by layer. Feature modules **must never** depend on other feature modules — shared logic moves to a `:core` module.
- **No Exceptions for Business Logic:** Expected failures use the `Result<T, E>` wrapper with typed errors. Raw exceptions are caught in the data layer and mapped to `DataError`.

## Tech Stack

DI: Koin · Backend: Firebase · Async: Coroutines + Flow · Navigation: Compose Navigation (type-safe) · Image Loading: Coil · Serialization: KotlinX Serialization · Logging: Kermit/Timber · Testing: JUnit5, Turbine, AssertK

## Module Layout

```
:app                          ← Entry point, Koin wiring, navigation setup
:build-logic                  ← Gradle convention plugins
:core:domain                  ← Shared models, repository interfaces, Result, base errors
:core:data                    ← Shared Firebase helpers, common DTOs
:core:presentation            ← Shared UI utilities (ObserveAsEvents, UiText)
:core:design-system           ← Reusable Compose components, theme, typography
:feature:<name>:domain        ← Feature-specific models and interfaces
:feature:<name>:data          ← Firebase implementations, DTOs, mappers
:feature:<name>:presentation  ← ViewModels, screens, MVI state/actions/events
```

## Dependency Rules

| Layer | May depend on |
|---|---|
| `presentation` | own `domain`, `core:domain`, `core:presentation`, `core:design-system` |
| `data` | own `domain`, `core:domain`, `core:data` |
| `domain` | `core:domain` only — never `data` or `presentation` |
| `:app` | everything |

Every layer may access `core:domain`. Features never import from other features.

## Firebase ops

Firestore composite indexes live in `firestore.indexes.json` at the repo root. Deploy with `firebase deploy --only firestore:indexes` (requires a `firebase.json` in the project — initialize once via `firebase init firestore` if missing). Any query combining `whereEqualTo` with `orderBy` on a different field needs an index entry here.

## Detailed Guidance — Skills

For specific patterns, the relevant skill below loads automatically. If you need to consult one directly, the file path is `.claude/skills/<name>/SKILL.md`:

- **autoai-architecture** — Adding new feature modules, convention plugins, dependency layering
- **autoai-mvi-presentation** — ViewModels, State/Action/Event, UI models, UiText, SavedStateHandle
- **autoai-compose-ui** — Smart/Dumb component split, stability, side effects, animations, previews
- **autoai-data-layer** — DataSource vs Repository, DTOs, mappers, Firebase `safeCall`, naming
- **autoai-koin-di** — Module structure, Firebase providers, `bind`, `singleOf`/`viewModelOf`, assembly
- **autoai-navigation** — `@Serializable` routes, `NavGraphBuilder` extensions, cross-feature callbacks
- **autoai-error-handling** — `Result` wrapper, `DataError`, `safeFirebaseCall`, `UiText` mapping
- **autoai-test-strategist** — Unit tests for ViewModels, data sources, mappers; Turbine for flows; Koin test modules
- **autoai-performance-specialist** — State hoisting, snapshotFlow, derivedStateOf, remember, side effect best practices

---
name: autoai-architecture
description: AutoAI project's Clean Architecture, module layout, and Firebase-first rules. Use this skill whenever the user is adding a new feature module, discussing module dependencies, working with build-logic convention plugins, or making any structural change involving :app, :core, or :feature modules — even when they don't explicitly mention "architecture". Trigger on any mention of feature creation, module setup, Gradle convention plugins, layer dependencies, or cross-feature coupling.
---

# AutoAI Architecture & Modules

## Approach

On hard problems in this area, think step by step and consider a few approaches before picking one. Walk through the reasoning explicitly — surface the tradeoffs, then commit to the choice. Skipping this on non-trivial work tends to produce solutions that look right but miss a constraint.

## Core Philosophy

- **Clean Architecture:** Strict layering: `presentation` → `domain` ← `data`. The **Domain** layer is innermost and must have **zero dependencies** on any other layer or framework.
- **Firebase-First Policy:** This project relies exclusively on **Firebase** (Auth, Firestore, Storage, etc.). **DO NOT** suggest or implement Ktor, Retrofit, Room, or SQLDelight unless explicitly requested.
- **Feature-Layered Modularization:** Code is split by feature first, then by layer within that feature.
- **Separation of Concerns:** Feature modules **must never** depend on other feature modules. Shared logic moves to the appropriate `:core` module.

## Module Layout

```
:app                          ← Entry point, dependency wiring (Koin), and navigation setup
:build-logic                  ← Gradle convention plugins for consistent build configuration
:core:domain                  ← Shared models, repository interfaces, base error types, Result classes
:core:data                    ← Shared Firebase helpers, common DTOs, global data sources
:core:presentation            ← Shared UI utilities (ObserveAsEvents, UiText, etc.)
:core:design-system           ← Reusable Compose components, colors, theme, typography
:feature:<name>:domain        ← Feature-specific domain models and repository interfaces
:feature:<name>:data          ← Firebase repository implementations, DTOs, mappers
:feature:<name>:presentation  ← ViewModels, screen composables, MVI state, actions, events
```

## Dependency Rules

| Layer | May depend on |
|---|---|
| `presentation` | `domain` (own feature), `core:domain`, `core:presentation`, `core:design-system` |
| `data` | `domain` (own feature), `core:domain`, `core:data` |
| `domain` | `core:domain` only — never `data` or `presentation` |
| `:app` | everything (wires all modules) |

Every layer and module may access `core:domain`.

## Key Technology Stack

- **DI:** Koin
- **Backend:** Firebase (Auth, Firestore, Cloud Functions)
- **Async:** Coroutines + Flow
- **Navigation:** Compose Navigation (type-safe)
- **Image Loading:** Coil
- **Serialization:** KotlinX Serialization (for Nav routes and DTOs)
- **Logging:** Kermit or Timber
- **Testing:** JUnit5, Turbine, AssertK

## Convention Plugins (`:build-logic`)

Use convention plugins in every module to avoid boilerplate:

- `android-application` — App module config
- `android-feature` — Bundles Android Library + Compose + Koin + shared feature deps
- `domain-module` — Pure Kotlin/KMP module with no Android/Firebase dependencies
- `firebase-config` — Configuration for Firebase services and Google Services plugin

## Checklist: Adding a New Feature

- [ ] Create `:feature:<name>:domain`, `:feature:<name>:data`, and `:feature:<name>:presentation` modules
- [ ] Apply appropriate convention plugins
- [ ] Ensure the `data` module implements interfaces defined in the `domain` module
- [ ] Verify that no cross-feature dependencies are introduced
- [ ] Ensure all Firebase interaction is encapsulated within the `data` layer

---
name: autoai-compose-ui
description: AutoAI's Compose UI patterns — Smart/Dumb component split, stability annotations, state ownership, side effects, animations, and previews. Use this skill whenever the user is writing or modifying a Compose screen, composable function, @Preview, animation, lazy list, or any UI code in the presentation layer. Trigger on any mention of Compose, composables, recomposition, screen layout, @Preview, LazyColumn, animations, Modifier, TextField, focus management, or imePadding.
---

# AutoAI Compose UI Patterns

## Approach

On hard problems in this area, think step by step and consider a few approaches before picking one. Walk through the reasoning explicitly — surface the tradeoffs, then commit to the choice. Skipping this on non-trivial work tends to produce solutions that look right but miss a constraint.

## Core Principle: UI is Dumb

The UI is strictly for rendering state and forwarding user actions. All logic lives in the ViewModel, domain, or data layer. Compose code contains **zero** business logic and **zero** data transformation.

## UI Component Structure (Strict Rule)

Every screen **must** be split into two distinct files.

### 1. Smart Wrapper (`<Screen>Screen.kt`)

- **Responsibility:** Data wiring and lifecycle observation
- **Injections:** Injects ViewModel via `koinViewModel()`
- **State:** Collects state using `collectAsStateWithLifecycle()`
- **Events:** Observes side effects (using `LaunchedEffect` or `ObserveAsEvents`)
- **Layout:** Contains **zero** layout code (no Rows, Columns, or Modifiers). It only calls the Dumb UI composable.

### 2. Dumb UI (`<Screen>Content.kt`)

- **Responsibility:** Visual representation
- **State:** Completely stateless. Takes `state`, `onAction/onEvent` lambda, and `SnackbarHostState` as parameters.
- **Layout:** Contains all layout code and `@Preview`
- **Form Standards:** For screens with forms/keyboards, the root element **must** be a `Box` with `Modifier.imePadding()`
- **Snackbar Positioning:** `SnackbarHost` must be placed inside the root `Box` with `Modifier.align(Alignment.TopCenter)` so the keyboard does not cover it
- **Focus Management:** Use `FocusRequester` to automatically focus the first input field upon screen launch

## Stability & Recomposition

- **Strong Skipping Mode:** Enabled by default
- **@Stable Annotation:** Only annotate state data classes if they contain unstable fields (`List`, `Map`, `Set`, or interfaces). If all fields are primitives or `String`, no annotation is needed.

```kotlin
@Stable
data class NoteListState(val notes: List<NoteUi> = emptyList())
```

## State Ownership

- **ViewModel is Truth:** All application state lives in the ViewModel. Do not use `remember` or `rememberSaveable` for app logic.
- **UI-Internal State:** Only framework-specific state (like `LazyListState` or `PagerState`) uses `remember`.
- **Derived State:** Use `derivedStateOf` only when Compose-internal state (like scroll position) drives a value. If it can be calculated in the ViewModel, do it there.

## Side Effects

- **ViewModel First:** If an action can be handled in the VM, do it there
- **Lifecycle Observation:** Extract lifecycle logic into dedicated composables (e.g., `ObserveLifecycle`) to keep Screen wrappers clean
- **LaunchedEffect:** Acceptable for UI-only effects (showing a Snackbar, scrolling to an item), but always question if it belongs in the VM

## Animations & Performance

- **Avoid Recomposition:** Prefer animating below the recomposition layer using `graphicsLayer` (for alpha, scale, rotation) or lambda-based modifiers (e.g., `offset { ... }`)
- **Deferred State Reads:** Pass state values as lambdas to modifiers when they drive animations. This defers execution to the layout/draw phase.

```kotlin
fun Modifier.animatedOffset(offsetProvider: () -> IntOffset) = offset { offsetProvider() }
```

## Lazy Layouts

- Always provide a `key` in `LazyColumn`/`LazyRow` when a unique identifier (like `id`) is available to optimize recomposition.

## Design System & Previews

- **Modifier Extensions:** Prefer plain extension functions. Do not make modifier extensions `@Composable`.
- **Slot APIs:** Use `@Composable () -> Unit` lambdas for flexible design system components (buttons, cards). Feature-level UI should use typed parameters.
- **Previews:** Every `Content.kt` must have a `@Preview` with realistic sample data, wrapped in the `AppTheme`.

## TextField & Accessibility

- **Input Flow:** Every keystroke dispatches an Action to the ViewModel. State is updated there and flowed back down.
- **Accessibility:** Every interactive element must have a `contentDescription` using string resources. Use `null` only for purely decorative elements.

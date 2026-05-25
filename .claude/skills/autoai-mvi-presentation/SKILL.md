---
name: autoai-mvi-presentation
description: AutoAI's strict MVI pattern for ViewModels — State, Action, Event, UI models, UiText, SavedStateHandle, and dispatcher rules. Use this skill whenever the user is creating or modifying a ViewModel, defining state/actions/events for a screen, mapping domain models to UI models, handling errors with UiText, or working with SavedStateHandle for process death. Trigger on any mention of ViewModel, MVI, state management, screen state, actions, events, UiText, or form recovery.
---

# AutoAI Presentation Layer (MVI)

We use a strict **MVI (Model-View-Intent)** pattern to ensure state predictability and facilitate testing.

## Approach

On hard problems in this area, think step by step and consider a few approaches before picking one. Walk through the reasoning explicitly — surface the tradeoffs, then commit to the choice. Skipping this on non-trivial work tends to produce solutions that look right but miss a constraint.

## Core MVI Components

Every screen consists of four key elements:

- **State (`<Screen>State`):** A single `data class` holding the entire UI state. Always update using `_state.update { it.copy(...) }` to ensure atomicity.
- **Action (`<Screen>Action`):** A sealed interface defining all user intents (e.g., `OnEmailChange`, `OnLoginClick`).
- **Event (`<Screen>Event`):** A sealed interface for one-time side effects (navigation, snackbars, hiding keyboard). Emitted via a `Channel`.
- **ViewModel (`<Screen>ViewModel`):** Holds the `StateFlow<State>`, processes `Action` calls, and dispatches `Event`s.

## UI Models & Mapping

Never pass Domain models directly to the UI if they require formatting.

- **UI Models:** Create models suffixed with `Ui` (e.g., `NoteUi`). These contain formatted strings (dates, currencies) ready for display.
- **UiText:** Use the `UiText` sealed interface for strings.
  - `UiText.DynamicString` — Raw data from a server (e.g., a username)
  - `UiText.StringResource` — Localized messages (e.g., errors from `R.string`)
- **Error Mapping:** Map errors from the Domain layer to `UiText` using extension functions (e.g., `DataError.toUiText()`).

## ViewModel Pattern & Process Death

```kotlin
class NoteListViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NoteListState(
        title = savedStateHandle["title"] ?: ""
    ))
    val state = _state.asStateFlow()

    private val _events = Channel<NoteListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: NoteListAction) {
        when (action) {
            is NoteListAction.OnTitleChange -> {
                savedStateHandle["title"] = action.title
                _state.update { it.copy(title = action.title) }
            }
            is NoteListAction.OnSaveClick -> {
                viewModelScope.launch { _events.send(NoteListEvent.NavigateBack) }
            }
        }
    }
}
```

## Naming Conventions

| Thing | Convention | Example |
|---|---|---|
| ViewModel | `<Screen>ViewModel` | `NoteListViewModel` |
| State | `<Screen>State` | `NoteListState` |
| Action | `<Screen>Action` | `NoteListAction` |
| Event | `<Screen>Event` | `NoteListEvent` |
| Screen wrapper | `<Screen>Screen` | `NoteListScreen` |
| Dumb UI | `<Screen>Content` | `NoteListContent` |
| UI model | `<Model>Ui` | `NoteUi`, `TodoItemUi` |

## Coroutine Dispatchers

- Do not inject `Dispatchers` unless the class is unit-tested and explicitly dispatches to `IO` or `Default`.
- For "main-safe" suspend functions, the ViewModel should not worry about the dispatcher.
- For blocking code (e.g., image compression), wrap it with `withContext(Dispatchers.IO)`.

## Checklist: Adding a New Screen

1. [ ] Define `State`, `Action`, and `Event` in the feature's presentation package
2. [ ] Implement the `ViewModel` with `StateFlow` and `Channel`
3. [ ] Create `<Screen>Content.kt` (Dumb UI) with `@Preview`
4. [ ] Create `<Screen>Screen.kt` (Smart Wrapper) using `koinViewModel()` and `ObserveAsEvents`
5. [ ] If the screen contains a form, use `SavedStateHandle` for key fields
6. [ ] Map Domain models to `Ui` models before they reach the `State`

# AutoAI Project Rules & Guidelines

You are an expert Android developer specializing in Jetpack Compose, Clean Architecture, MVI, Koin, and Firebase.
Whenever you generate code or review PRs for this project, you MUST strictly adhere to the following rules.

## 1. Core Architecture & Modules

### Core Philosophy

* **Clean Architecture:** Strict layering: `presentation` → `domain` ← `data`. The **Domain** layer is the innermost and must have **zero dependencies** on any other layer or framework.

* **Firebase-First Policy:** This project relies exclusively on **Firebase** (Auth, Firestore, Storage, etc.). **DO NOT** suggest or implement Ktor, Retrofit, Room, or SQLDelight unless explicitly requested.

* **Feature-Layered Modularization:** Code is split by feature first, then by layer within that feature.

* **Separation of Concerns:** Feature modules **must never** depend on other feature modules. If two features need to share logic, that logic must be moved to the appropriate `:core` module.

---
 
### Module Layout

```
:app ← Entry point, dependency wiring (Koin), and navigation setup 
:build-logic ← Gradle convention plugins for consistent build configuration 
:core:domain ← Shared models, repository interfaces, base error types, and Result classes 
:core:data ← Shared Firebase helpers, Common DTOs, and global data sources 
:core:presentation ← Shared UI utilities (ObserveAsEvents, UiText, etc.) 
:core:design-system ← Reusable Compose components, colors, theme, and typography 
:feature:\<name>:domain ← Feature-specific domain models and repository interfaces 
:feature:\<name>:data ← Firebase repository implementations, DTOs, and mappers 
:feature:\<name>:presentation ← ViewModels, screen composables, MVI state, actions, and events
```
---
 
### Dependency Rules & Layering
| Layer | May depend on |
|---|---|
| `presentation` | `domain` (own feature), `core:domain`, `core:presentation`, `core:design-system` |
| `data` | `domain` (own feature), `core:domain`, `core:data` |
| `domain` | `core:domain` only — never `data` or `presentation` |
| `:app` | everything (wires all modules) | 

**Every** layer and module may access `core:domain`.

---
 
### Key Technology Stack

* **DI:** Koin

* **Backend:** Firebase (Auth, Firestore, Cloud Functions)

* **Async:** Coroutines + Flow

* **Navigation:** Compose Navigation (Type-safe)

* **Image Loading:** Coil

* **Serialization:** KotlinX Serialization (for Nav routes and DTOs)

* **Logging:** Kermit or Timber

* **Testing:** JUnit5, Turbine, AssertK
---
 
### Convention Plugins (`:build-logic`)

To avoid boilerplate, use convention plugins in every module:

* `android-application`: App module config.

* `android-feature`: Bundles Android Library + Compose + Koin + shared feature deps.

* `domain-module`: Pure Kotlin/KMP module with no Android/Firebase dependencies.

* `firebase-config`: Specific configuration for Firebase services and Google Services plugin.
---

### Checklist: Adding a New Feature

* \[ ] Create `:feature:<name>:domain`, `:feature:<name>:data`, and `:feature:<name>:presentation` modules.

* \[ ] Apply appropriate convention plugins.

* \[ ] Ensure the `data` module implements interfaces defined in the `domain` module.

* \[ ] Verify that no cross-feature dependencies are introduced.

* \[ ] Ensure all Firebase interaction is encapsulated within the `data` layer.
---
 

## 2. Presentation Layer (MVI & Compose UI)

We use a strict **MVI (Model-View-Intent)** pattern to ensure state predictability and facilitate testing.

### Core MVI Components

Every screen consists of four key elements:

* **State (`<Screen>State`):** A single `data class` holding the entire UI state.

  * Always update using `_state.update { it.copy(...) }` to ensure atomicity.

* **Action (`<Screen>Action`):** A sealed interface defining all user intents (e.g., `OnEmailChange`, `OnLoginClick`).

* **Event (`<Screen>Event`):** A sealed interface for one-time side effects (e.g., navigation, showing a snackbar, or hiding the keyboard). Emitted via a `Channel`.

* **ViewModel (`<Screen>ViewModel`):** Holds the `StateFlow<State>`, processes `Action` calls, and dispatches `Event`s.

***

### UI Models & Mapping

Never pass Domain models directly to the UI if they require formatting.

* **UI Models:** Create specific models suffixed with `Ui` (e.g., `NoteUi`). These contain formatted strings (dates, currencies) ready for display.

* **UiText:** Use the `UiText` sealed interface for strings.

  * `UiText.DynamicString`: For raw data from a server (e.g., a username).

  * `UiText.StringResource`: For localized messages (e.g., errors from `R.string`).

* **Error Mapping:** Map errors from the Domain layer to `UiText` using extension functions (e.g., `DataError.toUiText()`).

***

###### ViewModel Pattern & Process Death
```kotlin

class NoteListViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NoteListState(
        title = savedStateHandle["title"] ?: "" // Recovery after process death
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
---
 
### Naming Conventions & Structure
| Thing | Convention | Example |
 |---|---|---|
| ViewModel | `<Screen>ViewModel` | `NoteListViewModel` |
| State | `<Screen>State` | `NoteListState` |
| Action | `<Screen>Action` | `NoteListAction` |
| Event | `<Screen>Event` | `NoteListEvent` |
| Screen wrapper | `<Screen>Screen` | `NoteListScreen` |
| Dumb UI | `<Screen>Content` | `NoteListContent` |
| UI model | `<Model>Ui` | `NoteUi`, `TodoItemUi` |

 ---
### Coroutine Dispatchers

* **Main Principle:** Do not inject `Dispatchers` unless the class is unit-tested and explicitly dispatches to `IO` or `Default`.

* For "main-safe" suspend functions, the ViewModel should not worry about the dispatcher.

* For blocking code (e.g., image compression), wrap it with `withContext(Dispatchers.IO)`.
---
 
### Checklist: Adding a New Screen

1. \[ ] Define `State`, `Action`, and `Event` in the feature's presentation package.

2. \[ ] Implement the `ViewModel` with `StateFlow` and `Channel`.

3. \[ ] Create `<Screen>Content.kt` (Dumb UI) with `@Preview`.

4. \[ ] Create `<Screen>Screen.kt` (Smart Wrapper) using `koinViewModel()` and `ObserveAsEvents`.

5. \[ ] If the screen contains a form, use `SavedStateHandle` for key fields.

6. \[ ] Map Domain models to `Ui` models before they reach the `State`.

---
# 3. Android / KMP Compose UI Patterns

## Core Principle: UI is Dumb

The UI is strictly for rendering state and forwarding user actions. All logic lives in the ViewModel, domain, or data layer. Compose code contains zero business logic and zero data transformation.

***

## UI Component Structure (Strict Rule)

Every screen **MUST** be split into two distinct files to maintain separation of concerns:

### 1. Smart Wrapper (`<Screen>Screen.kt`)

* **Responsibility:** Data wiring and lifecycle observation.

* **Injections:** Injects ViewModel via `koinViewModel()`.

* **State:** Collects state using `collectAsStateWithLifecycle()`.

* **Events:** Observes side effects (using `LaunchedEffect` or `ObserveAsEvents`).

* **Layout:** Contains **ZERO** layout code (no Rows, Columns, or Modifiers). It only calls the "Dumb UI" composable.

### 2. Dumb UI (`<Screen>Content.kt`)

* **Responsibility:** Visual representation.

* **State:** Completely stateless. Takes `state`, `onAction/onEvent` lambda, and `SnackbarHostState` as parameters.

* **Layout:** Contains all layout code and `@Preview`.

* **Form Standards:** For screens with forms/keyboards, the root element **MUST** be a `Box` with `Modifier.imePadding()`.

* **Snackbar Positioning:** `SnackbarHost` must be placed inside the root `Box` with `Modifier.align(Alignment.TopCenter)` so the keyboard does not cover it.

* **Focus Management:** Use `FocusRequester` to automatically focus the first input field upon screen launch.

***

## Stability & Recomposition

* **Strong Skipping Mode:** Enabled by default.

* **@Stable Annotation:** Only annotate state data classes if they contain unstable fields (e.g., `List`, `Map`, `Set`, or interfaces). If all fields are primitives or `String`, no annotation is needed.

  Kotlin

  ```
  @Stable // Required due to List
  data class NoteListState(val notes: List<NoteUi> = emptyList())
  ```

***

## State Ownership

* **ViewModel is Truth:** All application state lives in the ViewModel. Do not use `remember` or `rememberSaveable` for app logic.

* **UI-Internal State:** Only framework-specific state (like `LazyListState` or `PagerState`) uses `remember`.

* **Derived State:** Use `derivedStateOf` only when Compose-internal state (like scroll position) drives a value. If it can be calculated in the ViewModel, do it there.

***

## Side Effects

* **ViewModel First:** If an action can be handled in the VM, do it there.

* **Lifecycle Observation:** Extract lifecycle logic into dedicated composables (e.g., `ObserveLifecycle`) to keep Screen wrappers clean.

* **LaunchedEffect:** Acceptable for UI-only effects (e.g., showing a Snackbar or scrolling to an item), but always question if it belongs in the VM.

***

## Animations & Performance

* **Avoid Recomposition:** Prefer animating below the recomposition layer using `graphicsLayer` (for alpha, scale, rotation) or lambda-based modifiers (e.g., `offset { ... }`).

* **Deferred State Reads:** Pass state values as lambdas to modifiers when they drive animations. This defers execution to the layout/draw phase.

  Kotlin

  ```
  // Good: Deferred read
  fun Modifier.animatedOffset(offsetProvider: () -> IntOffset) = offset { offsetProvider() }
  ```

***

## Lazy Layouts

* **Keys:** Always provide a `key` in `LazyColumn/Row` when a unique identifier (like `id`) is available to optimize recomposition.

***

## Design System & Previews

* **Modifier Extensions:** Prefer plain extension functions. Do not make modifier extensions `@Composable`.

* **Slot APIs:** Use `@Composable () -> Unit` lambdas for flexible design system components (buttons, cards). Feature-level UI should use typed parameters.

* **Previews:** Every `Content.kt` must have a `@Preview` with realistic sample data, wrapped in the `AppTheme`.

***

## TextField & Accessibility

* **Input Flow:** Every keystroke dispatches an Action to the ViewModel. State is updated there and flowed back down.

* **Accessibility:** Every interactive element must have a `contentDescription` using string resources. Use `null` only for purely decorative elements.

## 4. Data Layer (Firebase & Repositories)

### Core Responsibility

The Data Layer is responsible for fetching, persisting, and mapping data. It hides the complexity of Firebase from the rest of the app.

***

### Data Source vs. Repository

To maintain high granularity and testability, we distinguish between these two:

* **Data Source:** Accesses a single source of truth (e.g., Firestore, FirebaseAuth, or local Preferences). Most classes here are Data Sources.

* **Repository:** Use this term **ONLY** when a class coordinates multiple data sources (e.g., fetching from Firestore and then updating a local Cache/DataStore).

***

### Firebase & Async Handling

Since we do not use Ktor or Room, we rely on **Firebase Coroutines Play Services**.

* **Avoid Listeners:** Prefer one-shot `suspend` calls using `.await()`.

* **Safe Call Blocks:** Use a global `safeCall` helper (defined in `:core:data`) to catch Firebase exceptions (e.g., `FirebaseFirestoreException`, `FirebaseAuthException`) and map them to our typed `Result<T, DataError>`.

```kotlin
// Example of a safe Firebase call
suspend fun getUser(id: String): Result<User, DataError.Remote> = safeCall {
    val document = firestore.collection("users").document(id).get().await()
    val dto = document.toObject<UserDto>() ?: throw Exception("Mapping error")
    dto.toUser()
}
```
***
### DTOs and Domain Models

* **DTO (Data Transfer Object):** Represents the data as it is stored in Firebase (e.g., `UserDto`). It uses Firebase-specific annotations if necessary.

* **Domain Model:** A pure Kotlin class used in the `domain` and `presentation` layers (e.g., `User`).

* **Mappers:** Always separate these layers. Mappers are simple extension functions in the `data` layer.

```
fun UserDto.toUser(): User = User(id = id, email = email)
fun User.toUserDto(): UserDto = UserDto(id = id, email = email)
```
***
### Implementations & Naming

Name implementations based on the service they wrap. **Do not** use the generic `Impl` suffix if a more descriptive name is available (e.g., use `Firestore` or `Auth` prefix).

| Thing | Convention | Example |
|---|---|---|
| Data source interface | <Entity><Service>DataSource | UserRemoteDataSource |
| Data source impl | <Service><Entity>DataSource | FirestoreUserDataSource |
| Repository interface | `<Entity>Repository` (multi-source only) | `NoteRepository` |
| Repository impl | <Service><Entity>Repository | `FirebaseAuthRepository` |
| DTO | `<Model>Dto` | `NoteDto` |
| Mapper | extension fun on source type | `fun NoteDto.toNote()` |
***
### Domain Layer Contracts (The Bridge)

* **Interfaces:** Every Data Source or Repository used by a ViewModel must have an interface defined in the `domain` module.

* **No Frameworks:** The `domain` layer must remain pure Kotlin. It should not know about Firebase, Google Services, or any other Android framework.
***
 ### Checklist: Adding a New Data Component

1. \[ ] Define the **Domain Model** in `:feature:<name>:domain`.

2. \[ ] Define the **Interface** (DataSource or Repository) in `:feature:<name>:domain`.

3. \[ ] Define feature-specific **Error Types** in `:feature:<name>:domain`.

4. \[ ] Create the **DTO** in `:feature:<name>:data`.

5. \[ ] Write **Mappers** (extension functions) in `:feature:<name>:data`.

6. \[ ] Implement the logic in `:feature:<name>:data` using Firebase `.await()` and `safeCall` wrappers.

7. \[ ] Register the implementation in the **Koin module** of the feature.

***
## 5. Dependency Injection (Koin)

### Principles

* **Layered Modules:** Create one Koin module per feature layer (e.g., `notesDataModule`, `notesPresentationModule`).

* **Centralized Assembly:** Modules are defined in their respective feature modules but **MUST** be assembled in the `:app` module's Application class.

* **Constructor Injection:** Use the constructor-reference overloads (`singleOf`, `viewModelOf`) by default. They are cleaner and handle parameter resolution automatically.

***

### Firebase & Core Injections

When providing Firebase instances, you **MUST** use the lambda syntax with explicit `getInstance()` calls to ensure safe, lazy initialization.

```kotlin

// core:data
val coreDataModule = module {
    // CORRECT: Explicit lazy initialization
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    
    // INCORRECT: Avoid using property access like Firebase.auth
    
    // Standard singleton via constructor reference
    singleOf(::SessionPreferences)
}
```
***
### Layer Module Definitions

#### Data Layer Module

Use `bind<Interface>()` to link implementations with their domain interfaces.

```kotlin
// feature:notes:data
val notesDataModule = module {
    singleOf(::FirestoreNoteDataSource) { bind<NoteRemoteDataSource>() }
    singleOf(::NoteRepositoryImpl) { bind<NoteRepository>() }
}
```
#### Presentation Layer Module
```kotlin
// feature:notes:presentation
val notesPresentationModule = module {
    viewModelOf(::NoteListViewModel)
    viewModelOf(::NoteDetailViewModel)
}
```
#### Assembly & Usage

##### Assembly in `:app`

Register all feature and core modules in your `Application` class:
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                coreDataModule,
                notesDataModule,
                notesPresentationModule,
                authDataModule,
                authPresentationModule
            )
        }
    }
}
```
##### Injection in Compose
Always use `koinViewModel()` in the **Smart Wrapper** (`<Screen>Screen.kt`). Never pass ViewModels down to children composables; pass the state and action lambdas instead.
```kotlin
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel = koinViewModel() // Proper injection
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // ...
}
```
***
### Scoping Rules Summary

| Scope | Preferred form | Fallback form | When to use |
|---|---|---|---|
| Singleton | `singleOf(::Impl)` | `single { ... }` | One instance for the app lifetime (repositories, HttpClient, DB) |
| ViewModel       | `viewModelOf(::MyViewModel)` | `viewModel { ... }` | ViewModel instances scoped to their lifecycle |
| Factory | `factoryOf(::Impl)` | `factory { ... }` | New instance on every injection (rare — prefer singleton or ViewModel) |
|     Manual    |           single { ... }          |                   |                                    Factory methods, Firebase instances, external libs                                    |
***
### Checklist: Adding DI for a New Feature

* \[ ] Define `<feature>DataModule` in the `data` module.

* \[ ] Define `<feature>PresentationModule` in the `presentation` module.

* \[ ] Use `bind<T>()` for implementations that fulfill a Domain interface.

* \[ ] Register all new modules in the `Application` class.

* \[ ] Ensure all Firebase instances are provided using the safe `single { ... }` lambda form.
***
## 6. Navigation

### Principles

* **Type-Safety:** Use `@Serializable` objects or data classes for all routes via `kotlinx.serialization`.

* **Feature Isolation:** Each feature defines its own `NavGraphBuilder` extension. Features **must never** import routes from other features.

* **Decoupled Navigation:** Intra-feature navigation uses the `NavController`. Cross-feature navigation is handled via **lambda callbacks** wired in the `:app` module.

* **State Management:** Avoid passing complex objects in routes. Pass **IDs** and fetch the necessary data in the destination ViewModel.
***
### Defining Routes

Routes should be defined in the feature's `presentation` module:

```kotlin
@Serializable 
data object LoginRoute // Use data object for no-arg routes

@Serializable 
data class NoteDetailRoute(val noteId: String) // Use data class for arguments
```
***
### Feature Nav Graph Structure

Each feature module exposes an extension function on `NavGraphBuilder`:
```kotlin
// feature:notes:presentation
fun NavGraphBuilder.notesGraph(
    navController: NavController,
    onNavigateToAuth: () -> Unit // Cross-feature callback
) {
    navigation<NotesGraphRoute>(startDestination = NoteListRoute) {
        composable<NoteListRoute> {
            NoteListScreen(
                onNavigateToDetail = { id -> 
                    navController.navigate(NoteDetailRoute(id)) 
                },
                onUnauthorized = onNavigateToAuth
            )
        }
        composable<NoteDetailRoute> { backStackEntry ->
            val route: NoteDetailRoute = backStackEntry.toRoute()
            NoteDetailScreen(noteId = route.noteId)
        }
    }
}
```
***
### Wiring and Backstack Management in `:app`

All feature graphs are assembled in the `:app` module. This is where you handle major flow transitions and backstack clearing.
```kotlin
// :app module
NavHost(
    navController = navController, 
    startDestination = AuthGraphRoute
) {
    authGraph(
        onLoginSuccess = {
            navController.navigate(HomeRoute) {
                // Clear backstack when moving from Auth to Home
                popUpTo(AuthGraphRoute) { inclusive = true }
            }
        }
    )
    
    notesGraph(
        navController = navController,
        onNavigateToAuth = {
            navController.navigate(AuthGraphRoute) {
                popUpTo(0) // Clear everything on logout
            }
        }
    )
}
```
***
## Naming Conventions
| Thing | Convention | Example |
|---|---|---|
| Nav route | `<Screen>Route` | `NoteListRoute`, `NoteDetailRoute` |
| Feature nav graph | `<feature>Graph(...)` on `NavGraphBuilder` | `notesGraph(...)` |
***
##### Checklist: Adding Navigation to a New Feature

1. \[ ] Define `@Serializable` routes in `feature:presentation`.

2. \[ ] Create a `NavGraphBuilder.<feature>Graph` extension function.

3. \[ ] Use `NavController` for navigation inside the feature.

4. \[ ] Define callbacks for any navigation that leaves the feature.

5. \[ ] Wire the new graph in the `:app` module's `NavHost`.

6. \[ ] Verify that backstack clearing is implemented for "Entry" or "Exit" points of the flow.

***
## 7. Error Handling

### Core Principles

* **No Exceptions for Business Logic:** Never throw exceptions for expected failures (e.g., wrong password, no internet). Use the `Result` wrapper.

* **Typed Errors:** Every failure must be represented by a typed error implementing the `Error` interface.

* **Layered Responsibility:** Raw exceptions (Firebase, Firestore, etc.) **must** be caught in the **Data Layer** and mapped to `DataError` before reaching the Domain or Presentation layers.
***
### Result Wrapper (`core:domain`)

A generic wrapper for any operation that can fail.
```kotlin
interface Error

sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.example.Error>(val error: E) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>
```
Included extensions: `.map()`, `.onSuccess()`, `.onFailure()`, and `.asEmptyResult()`.
***
### Shared Error Types (`core:domain`)

We categorize errors into `Network` (Remote) and `Local`:
```kotlin
sealed interface DataError : Error { 
enum class Remote : DataError { SERVICE\_UNAVAILABLE, UNAUTHORIZED, NOT\_FOUND, NO\_INTERNET, SERVER\_ERROR, UNKNOWN } 
enum class Local : DataError { DISK\_FULL, NOT\_FOUND, UNKNOWN } }
```
***
### Firebase Safe Call Helper (`core:data`)

To keep the Data Layer clean, use a helper to wrap Firebase Tasks and map exceptions:
```kotlin
suspend fun <T> safeFirebaseCall(block: suspend () -> T): Result<T, DataError.Remote> {
    return try {
        Result.Success(block())
    } catch (e: FirebaseAuthException) {
        Result.Error(DataError.Remote.UNAUTHORIZED)
    } catch (e: FirebaseFirestoreException) {
        Result.Error(DataError.Remote.SERVER_ERROR)
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Result.Error(DataError.Remote.UNKNOWN)
    }
}
```
***
### UiText & Mapping (`core:presentation`)

**Never hardcode error strings in ViewModels.** All errors must be mapped to string resources using `UiText`.
```kotlin
// In core:presentation or feature:presentation
fun DataError.toUiText(): UiText {
    return when (this) {
        DataError.Remote.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
        DataError.Remote.UNAUTHORIZED -> UiText.StringResource(R.string.error_auth_failed)
        else -> UiText.StringResource(R.string.error_unknown)
    }
}
```
Usage in ViewModel:
```kotlin
_state.update { it.copy(error = error.toUiText()) }
```
***

### Exception Handling Philosophy
| Exception origin | Catch in | Example |
|---|---|---|
| Firebase / Firestore | Data layer | Map to DataError.Remote |
| Auth Validation | Domain layer | Map to AuthError (e.g., INVALID_EMAIL) |
| Business logic | Domain layer | Return Result.Error with custom Enum |

***
### Checklist: Error Handling

* \[ ] Catch raw Firebase/framework exceptions in the **Data Layer**.

* \[ ] Ensure all repository functions return `Result<T, E>`.

* \[ ] Create a `.toUiText()` extension for every user-facing error.

* \[ ] Verify that ViewModels only deal with `UiText` and never raw Strings for errors.

* \[ ] Ensure `CancellationException` is rethrown in safe call blocks to keep Coroutines working correctly.

***

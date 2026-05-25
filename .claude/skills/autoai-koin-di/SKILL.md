---
name: autoai-koin-di
description: AutoAI's Koin DI conventions — module structure, Firebase instance providers, bind syntax, viewModelOf/singleOf usage, and Application assembly. Use this skill whenever the user is registering a class with Koin, creating or modifying a Koin module, wiring up dependency injection, or troubleshooting DI issues. Trigger on any mention of Koin, dependency injection, DI module, singleOf, viewModelOf, bind, koinViewModel, or registering Firebase instances.
---

# AutoAI Dependency Injection (Koin)

## Approach

On hard problems in this area, think step by step and consider a few approaches before picking one. Walk through the reasoning explicitly — surface the tradeoffs, then commit to the choice. Skipping this on non-trivial work tends to produce solutions that look right but miss a constraint.

## Principles

- **Layered Modules:** Create one Koin module per feature layer (e.g., `notesDataModule`, `notesPresentationModule`)
- **Centralized Assembly:** Modules are defined in their respective feature modules but **must** be assembled in the `:app` module's `Application` class
- **Constructor Injection:** Use the constructor-reference overloads (`singleOf`, `viewModelOf`) by default. They are cleaner and handle parameter resolution automatically.

## Firebase & Core Injections

When providing Firebase instances, you **must** use the lambda syntax with explicit `getInstance()` calls to ensure safe, lazy initialization.

```kotlin
// core:data
val coreDataModule = module {
    // CORRECT: Explicit lazy initialization
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }

    // INCORRECT: Avoid property access like Firebase.auth

    singleOf(::SessionPreferences)
}
```

## Layer Module Definitions

### Data Layer Module

Use `bind<Interface>()` to link implementations with their domain interfaces.

```kotlin
// feature:notes:data
val notesDataModule = module {
    singleOf(::FirestoreNoteDataSource) { bind<NoteRemoteDataSource>() }
    singleOf(::NoteRepositoryImpl) { bind<NoteRepository>() }
}
```

### Presentation Layer Module

```kotlin
// feature:notes:presentation
val notesPresentationModule = module {
    viewModelOf(::NoteListViewModel)
    viewModelOf(::NoteDetailViewModel)
}
```

### Assembly in `:app`

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

### Injection in Compose

Always use `koinViewModel()` in the **Smart Wrapper** (`<Screen>Screen.kt`). Never pass ViewModels down to children composables — pass state and action lambdas instead.

```kotlin
@Composable
fun NoteListScreen(
    viewModel: NoteListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // ...
}
```

## Scoping Rules Summary

| Scope | Preferred form | Fallback form | When to use |
|---|---|---|---|
| Singleton | `singleOf(::Impl)` | `single { ... }` | One instance for the app lifetime (repositories, HttpClient, DB) |
| ViewModel | `viewModelOf(::MyViewModel)` | `viewModel { ... }` | ViewModel instances scoped to their lifecycle |
| Factory | `factoryOf(::Impl)` | `factory { ... }` | New instance on every injection (rare — prefer singleton or ViewModel) |
| Manual | `single { ... }` | | Factory methods, Firebase instances, external libs |

## Checklist: Adding DI for a New Feature

- [ ] Define `<feature>DataModule` in the `data` module
- [ ] Define `<feature>PresentationModule` in the `presentation` module
- [ ] Use `bind<T>()` for implementations that fulfill a Domain interface
- [ ] Register all new modules in the `Application` class
- [ ] Ensure all Firebase instances are provided using the safe `single { ... }` lambda form

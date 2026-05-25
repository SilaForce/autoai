---
name: autoai-data-layer
description: AutoAI's data layer rules — DataSource vs Repository, Firebase safeCall, DTOs, mappers, and naming conventions for Firestore/Auth/Storage code. Use this skill whenever the user is writing Firebase code, creating a DataSource or Repository, defining a DTO, writing mappers, or working with Firestore/FirebaseAuth/FirebaseStorage calls. Trigger on any mention of Firebase, Firestore, FirebaseAuth, Storage, DataSource, Repository, DTO, safeCall, or "fetching data".
---

# AutoAI Data Layer (Firebase & Repositories)

## Approach

On hard problems in this area, think step by step and consider a few approaches before picking one. Walk through the reasoning explicitly — surface the tradeoffs, then commit to the choice. Skipping this on non-trivial work tends to produce solutions that look right but miss a constraint.

## Core Responsibility

The Data Layer is responsible for fetching, persisting, and mapping data. It hides the complexity of Firebase from the rest of the app.

## Data Source vs. Repository

- **Data Source:** Accesses a single source of truth (Firestore, FirebaseAuth, or local Preferences). Most classes here are Data Sources.
- **Repository:** Use this term **only** when a class coordinates multiple data sources (e.g., fetching from Firestore and then updating a local Cache/DataStore).

## Firebase & Async Handling

Since we do not use Ktor or Room, we rely on **Firebase Coroutines Play Services**.

- **Avoid Listeners:** Prefer one-shot `suspend` calls using `.await()`
- **Safe Call Blocks:** Use a global `safeCall` helper (defined in `:core:data`) to catch Firebase exceptions (`FirebaseFirestoreException`, `FirebaseAuthException`) and map them to our typed `Result<T, DataError>`.

```kotlin
suspend fun getUser(id: String): Result<User, DataError.Remote> = safeCall {
    val document = firestore.collection("users").document(id).get().await()
    val dto = document.toObject<UserDto>() ?: throw Exception("Mapping error")
    dto.toUser()
}
```

## DTOs and Domain Models

- **DTO (Data Transfer Object):** Represents the data as stored in Firebase (e.g., `UserDto`). Uses Firebase-specific annotations if necessary.
- **Domain Model:** A pure Kotlin class used in the `domain` and `presentation` layers (e.g., `User`).
- **Mappers:** Always separate these layers. Mappers are simple extension functions in the `data` layer.

```kotlin
fun UserDto.toUser(): User = User(id = id, email = email)
fun User.toUserDto(): UserDto = UserDto(id = id, email = email)
```

## Implementations & Naming

Name implementations based on the service they wrap. Do not use the generic `Impl` suffix if a more descriptive name is available.

| Thing | Convention | Example |
|---|---|---|
| Data source interface | `<Entity><Service>DataSource` | `UserRemoteDataSource` |
| Data source impl | `<Service><Entity>DataSource` | `FirestoreUserDataSource` |
| Repository interface | `<Entity>Repository` (multi-source only) | `NoteRepository` |
| Repository impl | `<Service><Entity>Repository` | `FirebaseAuthRepository` |
| DTO | `<Model>Dto` | `NoteDto` |
| Mapper | extension fun on source type | `fun NoteDto.toNote()` |

## Domain Layer Contracts (The Bridge)

- **Interfaces:** Every Data Source or Repository used by a ViewModel must have an interface defined in the `domain` module.
- **No Frameworks:** The `domain` layer must remain pure Kotlin. It should not know about Firebase, Google Services, or any other Android framework.

## Checklist: Adding a New Data Component

1. [ ] Define the **Domain Model** in `:feature:<name>:domain`
2. [ ] Define the **Interface** (DataSource or Repository) in `:feature:<name>:domain`
3. [ ] Define feature-specific **Error Types** in `:feature:<name>:domain`
4. [ ] Create the **DTO** in `:feature:<name>:data`
5. [ ] Write **Mappers** (extension functions) in `:feature:<name>:data`
6. [ ] Implement the logic in `:feature:<name>:data` using Firebase `.await()` and `safeCall` wrappers
7. [ ] Register the implementation in the **Koin module** of the feature

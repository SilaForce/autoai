# AutoAI

An Android app for car owners to track vehicles, costs, reminders, and chat with an AI assistant about their car. Built as a portfolio project exploring modern Android development end to end — Jetpack Compose, Clean Architecture, MVI, Firebase, and the Gemini API.

## What it does

- **Garage** — add multiple vehicles with make, model, year, fuel type, mileage, plate, and an optional photo. Mark one as active.
- **Home** — at-a-glance view of the active vehicle, monthly expense total, and the next upcoming reminder.
- **Costs** — log fuel, service, and other expenses, broken down by category with statistics over time.
- **Reminders** — schedule maintenance tasks (oil change, inspection, registration) with notifications.
- **AI Chat** — ask Gemini about your specific car: troubleshooting, when to service something, what a warning light means. The assistant has live context of your active vehicle (make, model, year, mileage, fuel type) so answers are tailored, not generic. It can also read your logged costs and existing reminders to ground its replies, and — when you've enabled **AI assistance for reminders** in Settings — it can create new reminders on your behalf straight from the conversation. Supports image attachments (e.g. snap a dashboard light).
- **Profile** — edit name, avatar, currency preference; delete account.

Localized in English and Bosnian.

## Tech stack

| Concern | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | Clean Architecture + MVI |
| DI | Koin |
| Async | Coroutines + Flow |
| Navigation | Compose Navigation (type-safe routes with `@Serializable`) |
| Networking | Ktor (for the NHTSA make/model autocomplete) |
| Backend | Firebase Auth, Firestore, Cloud Storage |
| AI | Google Gen AI SDK (Gemini) |
| Image loading | Coil |
| Serialization | KotlinX Serialization |
| Testing | JUnit, Turbine, AssertK |

## Architecture

Strict layering, feature-first modularization:

```
:app                          ← entry point, Koin wiring, nav setup
:core:domain (folded into :domain in this repo)
:domain                       ← models, repository interfaces, use cases, Result type
:data                         ← Firebase implementations, DTOs, mappers
```

- **`domain`** has zero framework dependencies. All errors are typed via a `Result<T, DataError>` wrapper; raw exceptions are caught and mapped in the data layer.
- **`data`** owns Firebase access. Each Firestore call goes through a `safeFirebaseCall` wrapper that maps `FirebaseFirestoreException` codes to typed `DataError`s.
- **`app`** holds presentation (ViewModels, screens, MVI state/actions/events). Every screen is split into a Smart `Screen` composable (connects to the VM) and a Dumb `Content` composable (pure UI, previewable).

Detailed conventions live in `CLAUDE.md` and the `.claude/skills/` directory — those are the playbooks for adding new features, modules, ViewModels, and so on.

## Running locally

You need:
- Android Studio Ladybug or newer
- JDK 11+
- A Firebase project (the app expects Firestore + Auth + Storage enabled)
- A Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey)

### 1. Clone and open

```bash
git clone https://github.com/SilaForce/autoai.git
cd AutoAI
```

### 2. Add your Firebase config

Create a Firebase Android app for the package `com.example.autoai`, then download the `google-services.json` and place it at:

```
app/google-services.json
```

This file is gitignored — keep it that way.

### 3. Add your Gemini API key

Create `local.properties` at the repo root (or edit the existing one) and add:

```properties
sdk.dir=/path/to/your/Android/sdk
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.5-flash
```

The key is read at build time and exposed via `BuildConfig.GEMINI_API_KEY`. `local.properties` is gitignored.

### 4. Deploy Firestore indexes (one-time)

```bash
firebase deploy --only firestore:indexes
```

This pushes the composite indexes in `firestore.indexes.json` (vehicles by `userId + make + model`, costs by `vehicleId + dateMillis`).

### 5. Build and run

```bash
./gradlew :app:installDebug
```

Or just hit Run in Android Studio with a device or emulator attached.

## Project structure

```
app/                  Presentation layer — ViewModels, screens, components, navigation
domain/               Pure Kotlin — models, use cases, repository interfaces
data/                 Firebase + Ktor implementations, DTOs, mappers
build-logic/          (none yet — convention plugins live inline in build files)
firestore.indexes.json    Firestore composite index definitions
```

Inside `app/src/main/java/com/example/autoai/presentation/features/` each feature is its own folder containing `*Screen`, `*Content`, `*ViewModel`, `*State`, `*Event`, `*SideEffect` files — the MVI quintet.

## Testing

Unit tests cover ViewModels, mappers, and validation logic:

```bash
./gradlew test
```

Turbine is used for asserting Flow emissions; AssertK for readable failure messages.

## Status

This is an in-progress personal project. Public for learning, code review, and inspiration — not yet on the Play Store. Issues and suggestions welcome.

## Author

[Amar Silajdžić](https://github.com/SilaForce) — Android developer based in Bosnia and Herzegovina.

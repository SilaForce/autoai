---
name: autoai-navigation
description: AutoAI's type-safe Compose Navigation conventions — @Serializable routes, per-feature NavGraphBuilder extensions, lambda callbacks for cross-feature navigation, and backstack management in :app. Use this skill whenever the user is adding a new screen route, creating a nav graph, wiring navigation between features, or managing the backstack. Trigger on any mention of navigation, NavController, NavGraphBuilder, @Serializable route, composable<Route>, popUpTo, startDestination, or cross-feature navigation.
---

# AutoAI Navigation

## Approach

On hard problems in this area, think step by step and consider a few approaches before picking one. Walk through the reasoning explicitly — surface the tradeoffs, then commit to the choice. Skipping this on non-trivial work tends to produce solutions that look right but miss a constraint.

## Principles

- **Type-Safety:** Use `@Serializable` objects or data classes for all routes via `kotlinx.serialization`
- **Feature Isolation:** Each feature defines its own `NavGraphBuilder` extension. Features **must never** import routes from other features.
- **Decoupled Navigation:** Intra-feature navigation uses the `NavController`. Cross-feature navigation is handled via **lambda callbacks** wired in the `:app` module.
- **State Management:** Avoid passing complex objects in routes. Pass **IDs** and fetch necessary data in the destination ViewModel.

## Defining Routes

Routes should be defined in the feature's `presentation` module:

```kotlin
@Serializable
data object LoginRoute // Use data object for no-arg routes

@Serializable
data class NoteDetailRoute(val noteId: String) // Use data class for arguments
```

## Feature Nav Graph Structure

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

## Wiring and Backstack Management in `:app`

All feature graphs are assembled in the `:app` module. This is where you handle major flow transitions and backstack clearing.

```kotlin
NavHost(
    navController = navController,
    startDestination = AuthGraphRoute
) {
    authGraph(
        onLoginSuccess = {
            navController.navigate(HomeRoute) {
                popUpTo(AuthGraphRoute) { inclusive = true }
            }
        }
    )

    notesGraph(
        navController = navController,
        onNavigateToAuth = {
            navController.navigate(AuthGraphRoute) {
                popUpTo(0)
            }
        }
    )
}
```

## Naming Conventions

| Thing | Convention | Example |
|---|---|---|
| Nav route | `<Screen>Route` | `NoteListRoute`, `NoteDetailRoute` |
| Feature nav graph | `<feature>Graph(...)` on `NavGraphBuilder` | `notesGraph(...)` |

## Checklist: Adding Navigation to a New Feature

1. [ ] Define `@Serializable` routes in `feature:presentation`
2. [ ] Create a `NavGraphBuilder.<feature>Graph` extension function
3. [ ] Use `NavController` for navigation inside the feature
4. [ ] Define callbacks for any navigation that leaves the feature
5. [ ] Wire the new graph in the `:app` module's `NavHost`
6. [ ] Verify that backstack clearing is implemented for entry/exit points of the flow

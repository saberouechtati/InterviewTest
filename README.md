# Legacy Odds Application Refactoring (Android Interview Test)

This project is a refactoring task for a legacy Android application that calculates the real-time status of odds during a sporting event. The goal is to restructure the project into a more easily maintainable, testable, and modern app.

## Task Goals

The primary objectives of this refactoring are:

1.  **Fix UI Issues:**
    *   List items are too big; they should not take full width/height.
    *   Display `sellIn`, `oddsValue`, and an image (fetched from URL) for each item, not just the name.
2.  **Improve Functionality:**
    *   Sort items by `sellIn` time.
    *   Add a button that calls an update function (equivalent to the legacy `calculateOdds()`) and refreshes the list.
3.  **Enhance Code Quality & Maintainability:**
    *   Refactor the code to be more maintainable.
    *   Implement a modern design pattern/architecture (Clean Architecture with MVVM).
    *   Promote clean code principles and reusability.
4.  **(Optional) Testing:** Add unit or UI tests.

## Technologies Used

*   **Kotlin:** Primary programming language.
*   **Jetpack Compose:** For building the UI declaratively.
*   **Clean Architecture:** To separate concerns into Data, Domain, and Presentation layers.
*   **MVVM (Model-View-ViewModel):** As the presentation layer pattern.
*   **Kotlin Coroutines & Flow:** For asynchronous operations and reactive data streams.
*   **Git & GitHub:** For version control and tracking changes.
*   **(Planned) Koin:** For dependency injection.
*   **(Planned) Coil:** For image loading.

## Build System Enhancements

Before diving deep into application code refactoring, foundational improvements were made to the project's build system.

*   **What was done:**
    *   **Migrated Gradle Build Scripts from Groovy to Kotlin DSL:**
        *   All `build.gradle` files (project-level and app-level) were renamed to `build.gradle.kts`.
        *   The syntax was updated to Kotlin, leveraging its type safety and better IDE support.
    *   **Upgraded Dependencies and Gradle Plugins:**
        *   The Android Gradle Plugin (AGP), Kotlin Gradle Plugin, and various library dependencies (AndroidX, Compose, Coroutines, etc.) were updated to their recent stable versions.
        *   Implemented a **Version Catalog (`libs.versions.toml`)** to centralize and manage dependency versions and plugin aliases, making the `build.gradle.kts` files cleaner and version management more robust.
    *   **Ensured Compatibility:** Verified compatibility between the updated AGP, Kotlin version, and the Jetpack Compose Compiler extension version.
*   **Why & Benefits (Task Goals Addressed):**
    *   **Improved Maintainability & Readability (Goal 3):**
        *   **Kotlin DSL:** Provides type safety, leading to earlier error detection during build script development. Offers significantly better IDE auto-completion, navigation, and refactoring capabilities compared to Groovy, especially for developers already familiar with Kotlin.
        *   **Version Catalog:** Centralizes dependency versions, reducing redundancy, preventing version conflicts, and making updates easier across multiple modules (if the project grows).
    *   **Enhanced Build Reliability & Performance:**
        *   Using up-to-date Gradle plugins and dependencies often brings performance improvements, bug fixes, and access to the latest features.
    *   **Modern Development Practices (Goal 3):** Aligning the build system with modern Android development standards, where Kotlin DSL and Version Catalogs are increasingly common.
    *   **Better Developer Experience:** The improved IDE support for Kotlin DSL makes working with build files less error-prone and more efficient.
    *   **Foundation for Future Growth:** A clean and modern build setup makes it easier to add new modules, libraries, or build configurations as the project evolves.

## Refactoring Steps & Decisions

This section details the step-by-step changes made to the application, the rationale behind them, and how they contribute to the project goals.

---

### Pre-Refactor State

*   The original application had UI rendering via Android Views and RecyclerView.
*   Business logic for calculating odds (`calculateOdds()`) was located directly in `MainActivity.java`.
*   Data items were represented by a `Bet.kt` class.
*   `ItemAdapter.java` was used for the RecyclerView.

---

### Step 1: Define Domain Model (`Odd.kt`) - `domain` layer

*   **What was done:**
    *   Created a Kotlin data class `com.betsson.interviewtest.domain.model.Odd`.
    *   This class (`id: String, name: String, sellIn: Int, oddsValue: Int, imageUrl: String?`) represents the core business entity for an betting odd, independent of data sources or UI.
*   **Why & Benefits (Task Goals Addressed):**
    *   **Maintainability & Clean Code:** Establishes a clear, immutable (where possible, `sellIn` and `oddsValue` are `var` as they change) representation of our core data.
    *   **Reusability:** This model is not tied to Android frameworks and can be reused if the business logic were to be shared (e.g., with a backend or another platform).
    *   **Foundation for Clean Architecture:** Defines the data structure that the domain and presentation layers will operate on.
    *   `id` was added to uniquely identify items, crucial for list updates in Compose and data management. `name` maps to `type`, `oddsValue` maps to `odds`, and `imageUrl` maps to `image` from the original `Bet.kt`.

---

### Step 2: Define Repository Interface (`OddsRepository.kt`) - `domain` layer

*   **What was done:**
    *   Created an interface `com.betsson.interviewtest.domain.repository.OddsRepository`.
    *   Defined methods:
        *   `getOddsStream(): Flow<List<Odd>>`: To provide a reactive stream of odds lists.
        *   `suspend fun triggerOddsUpdate()`: To command the underlying data source to update the odds based on business rules.
*   **Why & Benefits (Task Goals Addressed):**
    *   **Maintainability & Decoupling (Clean Architecture):** Abstracts the data source implementation from the domain and presentation layers. Use cases will depend on this interface, not a concrete implementation.
    *   **Testability:** Allows for easy mocking of the data layer during unit testing of use cases and ViewModels.
    *   **Reactive Updates:** Using `Flow` enables the UI to reactively update when odds change. (Addresses Goal 2 for list refresh).

---

### Step 3: Implement Initial Data Layer - `data` layer

#### 3.A: `OddsLogicProcessor.kt`

*   **What was done:**
    *   Created `com.betsson.interviewtest.data.helper.OddsLogicProcessor`.
    *   Moved the core calculation logic from `MainActivity.calculateOdds()` into `OddsLogicProcessor.processOddsUpdate(oddsList: List<Odd>): List<Odd>`.
    *   This method now takes a list of domain `Odd` objects and returns a *new* list with updated `sellIn` and `oddsValue` based on the original rules.
*   **Why & Benefits (Task Goals Addressed):**
    *   **Maintainability & Single Responsibility:** Centralizes the core business rule execution, removing it from the UI layer (`MainActivity`). `MainActivity` should not contain business logic.
    *   **Testability:** This pure function (or class method) can now be unit tested in isolation.
    *   **Clean Code:** Promotes immutability by returning a new list instead of mutating in place, which is generally safer and more predictable. (Addresses Goal 3).

#### 3.B: `OddsRepositoryImpl.kt`

*   **What was done:**
    *   Created `com.betsson.interviewtest.data.repository.OddsRepositoryImpl` implementing `OddsRepository`.
    *   Integrated `OddsLogicProcessor` to perform updates.
    *   Adapted the `getItemsFromNetwork()` logic from `MainActivity` into a private `getItemsFromDataSource()` method to provide the initial list of `Bet` objects.
    *   Implemented mapping from the source `Bet` objects to our domain `Odd` objects, including generating a more unique `id`.
    *   Uses a `MutableStateFlow<List<Odd>>` internally to hold and emit the current list of odds.
    *   Ensures that data emitted by `getOddsStream()` and after updates via `triggerOddsUpdate()` is sorted by `sellIn`.
*   **Why & Benefits (Task Goals Addressed):**
    *   **Clean Architecture Implementation:** Provides a concrete implementation for data retrieval and updates.
    *   **Separation of Concerns:** Handles data source interaction (currently mocked list, but could be a network call) and delegates business rule application to `OddsLogicProcessor`.
    *   **Reactive Data Source:** The `MutableStateFlow` allows the rest of the app (via `getOddsStream()`) to observe changes.
    *   **Sorting (Goal 2):** Implements the requirement to sort items by `sellIn` at the data source level.
    *   **Data Transformation:** Manages the conversion between raw data source models (`Bet`) and the application's domain models (`Odd`).

---

### Step 4: Define Use Cases - `domain` layer

*   **What was done:**
    *   Created two use case classes in the `com.betsson.interviewtest.domain.usecase` package:
        *   `GetSortedOddsStreamUseCase(oddsRepository: OddsRepository)`: Provides an `invoke()` operator that returns the `Flow<List<Odd>>` from the repository. This stream is expected to contain odds sorted by `sellIn`.
        *   `TriggerOddsUpdateUseCase(oddsRepository: OddsRepository)`: Provides a `suspend operator fun invoke()` that calls `oddsRepository.triggerOddsUpdate()` to initiate the odds calculation process.
*   **Why & Benefits (Task Goals Addressed):**
    *   **Clean Architecture & Single Responsibility:** Use cases encapsulate specific pieces of business logic/application-specific actions. They act as intermediaries between the Presentation layer (ViewModels) and the Data layer (Repositories).
    *   **Improved Readability & Maintainability:** Clearly defines the available operations in the domain. If the logic for getting or updating odds becomes more complex (e.g., involving multiple repositories or additional business rules before/after fetching data), that complexity would reside within the use case, keeping ViewModels cleaner.
    *   **Testability:** Use cases can be unit tested independently by mocking the repository they depend on.
    *   **Reusability:** These actions are now defined in a way that could be reused by different ViewModels or even other entry points into the domain logic (e.g., background services) if needed.
    *   Helps in achieving the "Refactor for maintainability" goal (Goal 3) by structuring the application logic cleanly.

---

### Step 5: Create ViewModel and UI State - `presentation` layer

*   **What was done:**
    *   Created `com.betsson.interviewtest.presentation.oddslist.OddsListUiState.kt`:
        *   `OddsListUiState` data class: Defines the complete, observable state for the odds list screen. It includes `isLoading` (Boolean for loading indicators), `odds` (a list of `OddItemUiModel` for display), and `error` (a nullable String for presenting error messages to the user).
        *   `OddItemUiModel` data class: Tailored specifically for UI representation, this model (`id`, `name`, `sellInText`, `oddsValueText`, `imageUrl`) ensures that data is formatted and structured exactly as the UI components require. For example, `sellInText` and `oddsValueText` can be pre-formatted strings.
        *   An extension function `Odd.toOddItemUiModel()`: Provides a clean and reusable way to map the `domain.model.Odd` objects to the UI-specific `OddItemUiModel`, decoupling the UI representation from the core domain logic.
    *   Created `com.betsson.interviewtest.presentation.oddslist.OddsListViewModel.kt`. Its key responsibilities and internal workings include:
        *   **Dependencies:** Takes `GetSortedOddsStreamUseCase` and `TriggerOddsUpdateUseCase` as constructor parameters. This adheres to dependency inversion, making the ViewModel testable and its dependencies explicit.
        *   **State Management:**
            *   `_uiState (MutableStateFlow)`: Privately holds the mutable current UI state (`OddsListUiState`). This is the single source of truth for the screen's state within the ViewModel.
            *   `uiState (StateFlow)`: Publicly exposes an immutable stream of `OddsListUiState`. UI components (Composables) will collect this flow to reactively update based on state changes.
        *   **Initialization (`init` block):**
            *   Calls `observeOdds()` immediately upon ViewModel creation. This ensures that the ViewModel starts loading and observing odds data as soon as it's instantiated, making data available to the UI quickly.
        *   **Observing Data (`observeOdds()` method):**
            *   Invokes `getSortedOddsStreamUseCase()` to obtain a `Flow<List<Odd>>` from the domain layer.
            *   Uses `.onEach` to process each new list of domain odds emitted by the flow:
                *   Maps domain `Odd` objects to `OddItemUiModel` using the defined mapper function.
                *   Updates `_uiState` with the new list of UI models, sets `isLoading` to `false` (as data is now available), and clears any pre-existing `error` messages.
            *   Uses `.catch` to gracefully handle any exceptions that might occur within the upstream flow (e.g., from the repository), updating `_uiState` with an appropriate error message and setting `isLoading` to `false`.
            *   Uses `.launchIn(viewModelScope)` to collect the flow within a coroutine that is automatically managed by the ViewModel's lifecycle. This prevents resource leaks by cancelling the collection when the ViewModel is cleared.
        *   **Handling User Actions (`onUpdateOddsClicked()` method):**
            *   This public function is invoked by the UI in response to a user interaction (e.g., clicking an "Update Odds" button).
            *   It sets `isLoading` to `true` in `_uiState` to provide immediate visual feedback to the user that an operation is in progress.
            *   Launches a new coroutine in `viewModelScope` to asynchronously call `triggerOddsUpdateUseCase()`, which in turn updates the odds in the repository.
            *   Includes `try-catch` block for robust error handling during the update operation. If an error occurs, `_uiState` is updated with the error message and `isLoading` is set to `false`.
            *   The subsequent data changes resulting from `triggerOddsUpdateUseCase` will be automatically picked up by the ongoing `observeOdds()` flow, which will then update the UI.

*   **Why & Benefits (Task Goals Addressed):**
    *   **MVVM Pattern & Separation of Concerns (Goal 3: Maintainability, Modern Design Pattern):**
        *   The ViewModel acts as the mediator between the UI (View) and the business logic (Model/Use Cases). It prepares and manages data for the UI, keeping the UI layer simple and focused on rendering.
        *   This clear separation makes the codebase easier to understand, modify, and test. UI logic is distinct from business logic.
    *   **Lifecycle Awareness & Resource Management (Goal 3: Maintainability):**
        *   By extending `androidx.lifecycle.ViewModel` and using `viewModelScope`, operations are automatically tied to the UI component's lifecycle. This prevents memory leaks and ensures that asynchronous operations are cancelled when the UI is no longer active.
    *   **Reactive UI Updates (Goal 2: List Refresh, Goal 3: Modern Design Pattern):**
        *   The use of `StateFlow` to expose `OddsListUiState` allows the Jetpack Compose UI to observe state changes reactively. When the state in the ViewModel updates, the UI will automatically and efficiently recompose only the necessary parts.
    *   **Testability (Goal 3: Maintainability, Goal 4: Testing):**
        *   ViewModels are significantly easier to unit test. Dependencies (UseCases) are injected and can be easily mocked, allowing for isolated testing of the ViewModel's logic and state transformations without needing an Android device or UI instrumentation.
    *   **Data Transformation & UI-Specific Models (Goal 1: Display all info, Goal 3: Clean Code):**
        *   `OddItemUiModel` and the mapping function allow data to be formatted and structured specifically for the UI's needs (e.g., "Sell In: 10" instead of just `10`). This keeps the domain model clean and focused on business entities, while the UI model caters to presentation details.
        *   This helps in displaying all required information (`sellIn`, `oddsValue`, `image`, `name`) as per the task requirements.
    *   **Centralized UI Logic (Goal 3: Maintainability):**
        *   All logic related to managing the state of the odds list screen (loading, error handling, data observation, user action responses) is centralized within the `OddsListViewModel`. This makes it the single source of truth for UI state, simplifying debugging and feature development.
    *   **Improved User Experience through State Management (Goal 2, Goal 3):**
        *   Explicitly managing `isLoading` and `error` states allows the UI to provide better feedback to the user (e.g., showing loading spinners, error messages), leading to a more robust and user-friendly application.

---

### Step 6: Create Composable UI - `presentation` layer

*   **What was done:**
    *   **Added Coil Dependency:** Integrated the Coil library for efficient image loading from URLs in Jetpack Compose.
    *   **`OddItemRow.kt` Composable:**
        *   Created a reusable Composable to display a single odd item.
        *   Uses `Card` for item structure and elevation.
        *   Displays `name`, `sellInText`, `oddsValueText`, and an image loaded via Coil's `AsyncImage`.
        *   `AsyncImage` is configured with placeholder and error drawables.
        *   Item dimensions are controlled using modifiers (`padding`, `size`) to prevent them from taking full screen width/height, addressing a key UI requirement.
    *   **`OddsListScreen.kt` Composable:**
        *   The main screen Composable that takes `OddsListViewModel` as a parameter.
        *   Observes `viewModel.uiState.collectAsState()` to reactively update the UI.
        *   Uses `Scaffold` for a standard Material Design layout including a `TopAppBar`.
        *   Delegates content display to a separate `OddsListContent` Composable.
    *   **`OddsListContent.kt` Composable (within `OddsListScreen.kt`):**
        *   Contains a "Update Odds" `Button` that calls `viewModel.onUpdateOddsClicked()`. The button's enabled state is tied to `uiState.isLoading`.
        *   Manages UI display based on `uiState`:
            *   Shows a `CircularProgressIndicator` during initial loading or list updates.
            *   Displays error messages if `uiState.error` is not null.
            *   Shows an empty state message if no odds are available.
            *   Uses `LazyColumn` to efficiently display the list of `OddItemRow`s. The `key` parameter in `items` is used for better performance during list updates.
        *   Includes `@Preview` Composable functions for `OddItemRow` and different states of `OddsListContent` to facilitate UI development and testing.
*   **Why & Benefits (Task Goals Addressed):**
    *   **Modern UI with Jetpack Compose (Goal 3: Modern Design Pattern):** Built the entire UI using Jetpack Compose, a declarative UI toolkit, leading to more concise and maintainable UI code.
    *   **Reactive UI (Goal 2: List Refresh):** The UI automatically updates when the `uiState` in the `OddsListViewModel` changes, thanks to `collectAsState()`.
    *   **Addressed Specific UI Requirements (Goal 1):**
        *   **Item Sizing:** `OddItemRow` is designed not to take full width/height.
        *   **Complete Information Display:** All required fields (`name`, `sellIn`, `oddsValue`, `image`) are now displayed for each item.
        *   **Image Loading:** Images are fetched from URLs and displayed.
    *   **Improved User Experience (Goal 1, Goal 2):**
        *   Clear visual feedback for loading states and error conditions.
        *   The "Update Odds" button provides the user with direct control to refresh the data.
    *   **Component Reusability:** `OddItemRow` is a reusable component.
    *   **Efficient List Display:** `LazyColumn` ensures that only visible items are composed and rendered, which is crucial for performance with potentially long lists.
    *   **Developer Productivity with Previews:** `@Preview` annotations allow for quick iteration and visualization of UI components in Android Studio without needing to run the app on a device/emulator for every small change.

---

### Step 7: Finalize UI Integration, Theming, System UI & Network Access

*   **Status:** Complete

#### What was done:

*   **`MainActivity` Final Integration & Refinement:**
    *   Ensured `MainActivity.kt` (now located in `presentation/MainActivity.kt`) correctly uses `setContent` to host the main Composable UI (`AppMainTheme` wrapping `OddsListScreen`).
    *   Applied the newly defined `AppMainTheme` at the root of the Composable tree within `MainActivity`'s `setContent` block.
    *   Verified that `OddsListViewModel` is correctly provided to `OddsListScreen` using `by viewModels()` with the `OddsListViewModelFactory` for proper lifecycle management and dependency provision.

*   **`AndroidManifest.xml` Updates:**
    *   Updated the `android:name` attribute for the `<activity>` tag corresponding to `MainActivity` to reflect its new package path: `.presentation.MainActivity`.
    *   **Added the `<uses-permission android:name="android.permission.INTERNET" />` permission.** This is essential for enabling network operations, such as fetching images from URLs using the Coil library for display in `OddItemRow`.

*   **Material 3 Theming Setup:**
    *   Created and configured standard theme files (`Color.kt`, `Theme.kt`, `Type.kt`) within the `presentation/theme` package.
    *   Defined distinct `LightColorScheme` and `DarkColorScheme` using custom application colors, supporting both light and dark modes.
    *   Integrated dynamic color theming for Android 12+ devices.

*   **System UI Modernization (Status Bar in `Theme.kt`):**
    *   Replaced deprecated `window.statusBarColor` access with modern `WindowCompat` APIs.
    *   Enabled edge-to-edge display using `WindowCompat.setDecorFitsSystemWindows(window, false)`.
    *   Implemented dynamic setting of status bar icon colors using `WindowCompat.getInsetsController().isAppearanceLightStatusBars`.
    *   Verified `Scaffold` and `TopAppBar` correctly handle system insets.

#### Why & Benefits (Task Goals Addressed):

*   **Enable Core App Functionality (Addresses Goal 3 - Modern Design Pattern, implicitly all goals):**
    *   The `INTERNET` permission is fundamental for image loading from network sources (via Coil), which is a key feature of the odds display. Without it, images would fail to load.
*   **Correct Application Functionality & Structure (Addresses all Goals implicitly):**
    *   Updating `AndroidManifest.xml` for `MainActivity`'s location and necessary permissions is critical for the app to launch and function correctly.
*   **Enhance Visual Appeal & User Experience (Addresses Goal 3 - Modern Design Pattern):**
    *   Displaying images alongside odds data significantly enhances the UI.
    *   Material 3 theming and edge-to-edge display provide a modern, polished user interface.
*   **Adherence to Best Practices & Future-Proofing (Addresses Goal 1 - Maintainability, Goal 3 - Modern Design Pattern):**
    *   Using `WindowCompat` for system UI styling is the current Android best practice.
    *   Explicitly declaring necessary permissions is a fundamental aspect of Android development.


---


*(README will be updated as more steps are completed)*
    
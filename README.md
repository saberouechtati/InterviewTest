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
    *(README will be updated as more steps are completed)*
    
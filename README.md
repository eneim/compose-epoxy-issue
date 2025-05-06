# Demonstrate an issue when using Jetpack Compose with Epoxy library

Also: https://kotlinlang.slack.com/archives/CJLTWPH7S/p1746449592633629

Demo:

https://github.com/user-attachments/assets/716295d8-4b7c-4b1e-8fef-d8c86a4376bd

### Background

- Under some conditions, AndroidComposeView instances will be cached for reuse, and their Composition (a `WrappedComposition`) are kept active to reduce the performance impact.
  - The performance impact in question is the overhead caused by Composition creation. By skipping the decomposition and reusing the Composition, such overhead is reduced.
- The current implementation of this strategy has some issues, which are discussed below.

### The issue [1]: inconsistent `WrappedComposition.addedToLifecycle`

- At [this line of code](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/platform/Wrapper.android.kt;l=118?q=Wrapper.android.kt), we can see that `WrappedComposition` will only be set to at most one non-null `addedToLifecycle`. If an AndroidComposeView is reused in a different Lifecycle, its `WrappedComposition` will not be updated to the new Lifecycle. This means the internal of `WrapperComposition` may follow the wrong `Lifecycle`.
- It is important to note that `addedToLifecycle` registers `WrappedComposition` as a LifecycleObserver; therefore, an inactive Lifecycle may affect the most recent UI. The issue [2] below mentions this effect.

### The issue [2]: leaking `WrappedComposition.addedToLifecycle`

- As a direct effect of issue [1], when `AndroidComposeView` is detached from the current Lifecycle, but not decomposed, its `WrappedComposition` is active and holds an active `Lifecycle`. If this `Lifecycle` belongs to a `Fragment` already destroyed, the `Lifecycle` instance is kept in memory and leaked.

### The issue [3]: Re-used WrappedComposition may dispose and cause empty UI

- Considering the case an `AndroidComposeView` was added to a `Lifecycle A`, then it is released to a Pool, then later re-used in `Lifecycle B` -> if `Lifecycle A` is destroyed, the `WrappedComposition` will `dispose` and the UI is gone.

| Expected behavior: UI is rendered | UI is gone |
|--|--|
|![Screenshot_20250506_145214](https://github.com/user-attachments/assets/fdff30d7-d1ce-4978-a759-181bfde3203b)|![Screenshot_20250506_145142](https://github.com/user-attachments/assets/9a5636fe-28fc-448f-af1b-07ee057ca5bb)|

- We can see that, the Composition's `addedToLifecycle` is destroyed and it clear the UI accordingly.

- An example of such a scenario: multiple Fragments share an Activity-bound RecycledViewPool, and each Fragment has a RecyclerView whose children are implemented using Jetpack Compose UI. This example is well-known in Apps migrating from RecyclerView-based UI to Compose UI.

# shared

Shared Kotlin Multiplatform module.

## Responsibilities

- API client (Ktor)
- Models and pagination
- Dependency injection (Koin)
- Shared UI (Compose Multiplatform)
- Activity charts
- Platform abstraction for credentials and date/time

## Source sets

- `commonMain/` - shared code
- `androidMain/` - Android HTTP engine and credential storage
- `desktopMain/` - Desktop HTTP engine and credential storage
- `iosMain/` - iOS HTTP engine and credential storage

## Key paths

### commonMain

- `shared/src/commonMain/kotlin/.../data/MemosRepository.kt` - paginated API calls
- `shared/src/commonMain/kotlin/.../model/Memo.kt` - API models
- `shared/src/commonMain/kotlin/.../di/SharedModule.kt` - Koin wiring
- `shared/src/commonMain/kotlin/.../ui/MemosApp.kt` - shared UI root
- `shared/src/commonMain/kotlin/.../ui/MemosViewModel.kt` - state + actions
- `shared/src/commonMain/kotlin/.../ui/components/ContributionGrid.kt` - activity charts
- `shared/src/commonMain/kotlin/.../config/*` - shared config types and constants
- `shared/src/commonMain/kotlin/.../network/HttpClientProvider.kt` - expect HTTP client

### androidMain

- `shared/src/androidMain/kotlin/.../network/HttpClientProvider.kt` - OkHttp engine
- `shared/src/androidMain/kotlin/.../config/CredentialsStore.kt` - SharedPreferences
- `shared/src/androidMain/kotlin/.../config/AndroidContextHolder.kt` - context holder
- `shared/src/androidMain/kotlin/.../config/DateProvider.kt` - local date

### desktopMain

- `shared/src/desktopMain/kotlin/.../network/HttpClientProvider.kt` - CIO engine
- `shared/src/desktopMain/kotlin/.../config/CredentialsStore.kt` - Preferences
- `shared/src/desktopMain/kotlin/.../config/DateProvider.kt` - local date

### iosMain

- `shared/src/iosMain/kotlin/.../network/HttpClientProvider.kt` - Darwin engine
- `shared/src/iosMain/kotlin/.../config/CredentialsStore.kt` - NSUserDefaults
- `shared/src/iosMain/kotlin/.../config/DateProvider.kt` - NSCalendar date

## Entry points

- `MemosApp` - shared UI root
- `MemosViewModel` - UI state and actions
- `MemosRepository` - API access

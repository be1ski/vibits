# План рефакторинга TEA кэша (кристально чистый)

**Дата:** 28 января 2026
**Цель:** Перенести кэширование activity data из Compose‑слоя в TEA State безопасно и предсказуемо, сохранив UX и консистентность данных.
**Источник требований:** `REFACTORING_TEA_CACHE.md` + патч `refactor-tea-cache.patch`.

## Quick Summary

**Проблема:** Бизнес-логика кэширования размазана между UI (remember), DI (cache), и async (LaunchedEffect) → race conditions, stale data, flashing UI.

**Решение:** Единый источник истины в TEA (HabitsState) → UI read-only → все вычисления в EffectHandler.

**Ключевые изменения:**
- `ActivityCacheKey(range, mode, appMode)` — ключ включает AppMode
- `memosRevision: Int` в MemosState — явное версионирование
- `isInitialLoading = false` — prewarm включает флаг
- Prewarm всех диапазонов (weeks/months/quarters/years)
- Никогда не очищать кэш (кроме смены AppMode)
- Success rate в EffectHandler, не в UI

**Результат:** устранены известные race conditions и flashing из TC‑01..TC‑10, консистентность данных обеспечивается архитектурой.

## 📋 Execution Plan (Краткий)

1. **Добавить memosRevision** в MemosState + инкремент в MemosReducer
2. **Добавить поля в HabitsState** (cache, flags) + новые Actions/Effects
3. **Написать failing tests** для всех TC-01..TC-10
4. **Реализовать по приоритетам** P0 → P1 → P2 → P3 (каждый TC = отдельный коммит)
5. **Интегрировать UI** (удалить remember-блоки, добавить LaunchedEffect)
6. **Удалить старый код** (remember-функции, DI cache)
7. **Ручное тестирование** всех сценариев
8. **Создать PR** с auto-merge squash

**Ожидаемое время:** [Не даём оценок]
**Ожидаемая сложность:** Высокая (архитектурный рефакторинг)
**Риск регрессий:** Средний (покрыто тестами + manual QA)

---

## 0) Базовые принципы (не нарушать)

1) **Единый источник истины**
   - Все расчёты activity data и success rate — только из TEA (HabitsState + Effects).
   - UI **read‑only** для activity data: никаких кэшей/пересчётов weekData/successRate в Compose.

2) **Никакой полной очистки кэша**
   - При пересчёте данные должны оставаться доступными (no empty/black flashes).
   - Используем флаги `needsCacheRefresh` / `isRecalculating` вместо `clear()`.

3) **Атомарные обновления**
   - `UpdateActivityData` заменяет запись для конкретного ключа одним шагом.

4) **Стабильные ключи**
   - Ключ кэша включает `ActivityRange + ActivityMode + AppMode`.
   - Обновление должно привязываться к смене **AppMode** и **версии memos**, а не к первому имени memo.

5) **TDD‑first**
   - Любая правка идёт через красный тест, затем зелёный, затем аккуратный рефактор.
   - Каждый баг из TC‑01..TC‑10 — отдельный тест и отдельный коммит.

---

## 0.1) Критические моменты (читать перед началом!)

### Частые ошибки которые нельзя допустить:

1. **isInitialLoading по умолчанию = false**
   - Неправильно: `val isInitialLoading: Boolean = true`
   - Правильно: `val isInitialLoading: Boolean = false`
   - Причина: prewarm сам включает флаг, иначе есть риск бесконечного loader при пустых memos

2. **Не очищать кэш при memo операциях**
   - Неправильно: `activityDataCache = emptyMap()` в `MemoCreated/Updated/Deleted`
   - Правильно: только `needsCacheRefresh = true`
   - Причина: Очистка → UI видит null → показывает empty state → мигание

3. **AppMode ОБЯЗАТЕЛЬНО в ActivityCacheKey**
   - Неправильно: `ActivityCacheKey(range, mode)`
   - Правильно: `ActivityCacheKey(range, mode, appMode)`
   - Причина: TC-10 — данные Demo и Offline не должны смешиваться

4. **memosRevision в MemosState ОБЯЗАТЕЛЕН**
   - Обязательно: добавить поле `val memosRevision: Int = 0`
   - Обязательно: инкрементировать при `MemosLoaded`, `ResetForModeChange`
   - Причина: TC-10 — memos могут совпадать по именам между режимами

5. **Emit ТОЛЬКО после withContext**
   - Неправильно: `withContext { emit(...) }`
   - Правильно: `val results = withContext { ... }; results.forEach { emit(...) }`
   - Причина: TC-04 — Flow invariant violation → infinite loader

6. **Prewarm запускается в одном месте (app shell)**
   - Неправильно: дублировать prewarm в HabitsScreen и PostsScreen
   - Правильно: один LaunchedEffect в app shell, оба экрана только читают state
   - Причина: Дублирование приводит к повторным вычислениям и гонкам

### Контрольный чеклист перед коммитом:

```kotlin
// ПРАВИЛЬНАЯ структура HabitsState:
data class HabitsState(
  val activityDataCache: Map<ActivityCacheKey, CachedActivityData> = emptyMap(),
  val isRecalculating: Set<ActivityCacheKey> = emptySet(),
  val needsCacheRefresh: Boolean = false,
  val isInitialLoading: Boolean = false,
)

// ПРАВИЛЬНЫЙ ActivityCacheKey:
data class ActivityCacheKey(
  val range: ActivityRange,
  val mode: ActivityMode,
  val appMode: AppMode, // ← ВАЖНО: appMode включён!
)

// ПРАВИЛЬНЫЕ LaunchedEffect ключи и условие:
LaunchedEffect(appMode, memosRevision, habitsState.needsCacheRefresh, habitsState.isInitialLoading) {
  val shouldPrewarm =
    !habitsState.isInitialLoading &&
      (habitsState.needsCacheRefresh || habitsState.activityDataCache.isEmpty())
  if (memos.isNotEmpty() && shouldPrewarm) {
    // request prewarm
  }
}

// ПРАВИЛЬНАЯ структура MemosState:
data class MemosState(
  val memos: List<Memo> = emptyList(),
  val memosRevision: Int = 0, // ← ВАЖНО: добавить!
)
```

---

## 1) Подготовка тестов (фаза Red)

### 1.1 Юнит‑тесты (shared/commonTest)

Добавить/обновить тесты для:
- **TC‑01:** Race condition success rate
- **TC‑03, TC‑06, TC‑07:** кэш не очищается при инвалидировании/обновлении
- **TC‑04:** Prewarm завершает поток, `PrewarmCompleted` приходит последним
- **TC‑05:** prewarm охватывает все недели от earliest memo до текущей
- **TC‑10:** смена AppMode должна инвалидировать/перезапускать кэш
- **MemosRevision:** инкремент при обновлении списка memos

### 1.2 Compose‑тесты (при необходимости в android/desktop)

- **TC‑02:** позиция даты не прыгает при появлении/исчезновении бейджа
- **TC‑09:** в POSTS mode нет success rate placeholder

**Важно:** строки в тестах — литералы (без const), согласно правилам проекта.

---

## 2) Дизайн контракта кэша (фаза Design)

### 2.1 Структуры

**Domain models** (уже существуют):
```kotlin
// shared/src/.../app/domain/model/ActivityRange.kt
sealed class ActivityRange {
  data class Week(val startDate: LocalDate) : ActivityRange()
  data class Month(val year: Int, val month: kotlinx.datetime.Month) : ActivityRange()
  data class Quarter(val year: Int, val index: Int) : ActivityRange()
  data class Year(val year: Int) : ActivityRange()
}

// shared/src/.../app/domain/model/ActivityMode.kt
enum class ActivityMode { HABITS, POSTS }
```

**Новые структуры для HabitsState** (в `feature/habits/presentation/`):
```kotlin
// Ключ кэша
data class ActivityCacheKey(
  val range: ActivityRange,
  val mode: ActivityMode,
  val appMode: AppMode, // чтобы не смешивать Demo/Offline/Online
)

// Закэшированные данные
data class CachedActivityData(
  val weekData: ActivityWeekData,             // данные по неделям
  val configTimeline: List<HabitsConfigEntry>, // конфиг timeline
  val successRate: SuccessRateData?,           // success rate (только для HABITS)
)

// Добавить в HabitsState:
data class HabitsState(
  // ... existing fields ...

  // Кэш activity data
  val activityDataCache: Map<ActivityCacheKey, CachedActivityData> = emptyMap(),

  // Множество ключей, для которых идёт пересчёт
  val isRecalculating: Set<ActivityCacheKey> = emptySet(),

  // Флаг необходимости полного обновления кэша
  val needsCacheRefresh: Boolean = false,

  // Флаг первоначальной загрузки (prewarm не завершён)
  val isInitialLoading: Boolean = false,
)
```

### 2.2 Инвалидация

**Правило:** Никогда не очищать кэш полностью (`activityDataCache = emptyMap()`), **кроме** смены AppMode.

**Стратегия инвалидации:**

1. **Операции с memos** (`MemoCreated`, `MemoUpdated`, `MemoDeleted`):
   ```kotlin
   is HabitsAction.MemoCreated/Updated/Deleted -> {
     state { copy(needsCacheRefresh = true) }
     // НЕ effect(InvalidateCache) — пусть LaunchedEffect обработает
   }
   ```
   - Выставляем флаг `needsCacheRefresh = true`
   - Старый кэш остаётся в state
   - LaunchedEffect видит флаг и триггерит prewarm

2. **Смена AppMode** (оркестрация в app shell):
   ```kotlin
   // В app shell (например, AppRoot.kt или главный Composable):
   @Composable
   fun AppRoot(...) {
     val appState by appFeature.state.collectAsState()
     val habitsState by habitsFeature.state.collectAsState()
     val prevAppMode by rememberPrevious(appState.appMode)

     // При смене appMode инвалидируем habits cache
     LaunchedEffect(appState.appMode) {
       if (prevAppMode != null && prevAppMode != appState.appMode) {
         onHabitsAction(HabitsAction.InvalidateAllCache)
       }
     }
     // ... остальной UI
   }

   // Helper функция:
   @Composable
   fun <T> rememberPrevious(value: T): T? {
     val ref = remember { mutableStateOf<T?>(null) }
     val prev = ref.value
     LaunchedEffect(value) { ref.value = value }
     return prev
   }

   // В HabitsReducer:
   is HabitsAction.InvalidateAllCache -> {
     state {
       copy(
         activityDataCache = emptyMap(), // OK здесь, т.к. меняем источник данных
         isRecalculating = emptySet(),
         needsCacheRefresh = true,
         isInitialLoading = true,
       )
     }
   }
   ```
   - При смене режима (Demo ↔ Offline) данные из другого источника
   - Можно полностью очистить кэш, т.к. старые данные неактуальны
   - LaunchedEffect с ключом `appMode` в app shell перезапустит prewarm

3. **Ручная инвалидация** (оставляем как API, но не используем в UI):
   ```kotlin
is HabitsAction.InvalidateCache -> {
  state {
    copy(
      isRecalculating = isRecalculating + ActivityCacheKey(action.range, action.mode, action.appMode),
      needsCacheRefresh = false, // сбрасываем флаг
    )
  }
  effect(HabitsEffect.RecalculateActivityData(action.range, action.mode, action.appMode, action.memos))
}
   ```

### 2.3 Триггеры и версионирование

**Решение:** использовать явную версию memos из `MemosState` + `appMode` + флаги HabitsState.

- В `MemosState` добавить `memosRevision: Int`.
- В `MemosReducer` инкрементировать `memosRevision` каждый раз, когда меняется список `memos` (например: `MemosLoaded`, `ResetForModeChange`).
- В UI использовать `LaunchedEffect(appMode, memosRevision, needsCacheRefresh, isInitialLoading)` с условием `shouldPrewarm`.

```kotlin
LaunchedEffect(appMode, memosRevision, habitsState.needsCacheRefresh, habitsState.isInitialLoading) {
  val shouldPrewarm =
    !habitsState.isInitialLoading &&
      (habitsState.needsCacheRefresh || habitsState.activityDataCache.isEmpty())
  if (memos.isNotEmpty() && shouldPrewarm) {
    onAction(HabitsAction.RequestPrewarmAllRanges(memos = memos, appMode = appMode))
  }
}
```

Это устраняет баг TC‑10 (memos могут совпадать по именам между режимами) и гарантирует пересчёт при любых изменениях memos.

### 2.4 Новые Actions и Effects

**Новые HabitsAction:**
```kotlin
sealed interface HabitsAction {
  // ... existing actions ...

  /** Запуск prewarm всех диапазонов */
  data class RequestPrewarmAllRanges(
    val memos: List<Memo>,
    val appMode: AppMode,
  ) : HabitsAction

  /** Обновление кэша для конкретного ключа (результат расчёта) */
  data class UpdateActivityData(
    val range: ActivityRange,
    val mode: ActivityMode,
    val appMode: AppMode,
    val weekData: ActivityWeekData,
    val configTimeline: List<HabitsConfigEntry>,
    val successRate: SuccessRateData?,
  ) : HabitsAction

  /** Prewarm завершён (все диапазоны обработаны) */
  data object PrewarmCompleted : HabitsAction

  /** Инвалидация всего кэша (при смене AppMode) */
  data object InvalidateAllCache : HabitsAction

  /** Ручная инвалидация конкретного ключа */
  data class InvalidateCache(
    val range: ActivityRange,
    val mode: ActivityMode,
    val appMode: AppMode,
    val memos: List<Memo>,
  ) : HabitsAction
}
```

**Новые HabitsEffect:**
```kotlin
sealed interface HabitsEffect {
  // ... existing effects ...

  /** Выполнение prewarm всех доступных диапазонов */
  data class RunPrewarmAllRanges(
    val memos: List<Memo>,
    val appMode: AppMode,
  ) : HabitsEffect

  /** Пересчёт данных для конкретного ключа */
  data class RecalculateActivityData(
    val range: ActivityRange,
    val mode: ActivityMode,
    val appMode: AppMode,
    val memos: List<Memo>,
  ) : HabitsEffect
}
```
Примечание: межфичевую оркестрацию делать на уровне app shell (AppRoot/VibitsAppScaffold), а не через AppEffectHandler.

---

## 3) Реализация с TDD (фаза Green, по одному багу)

**Порядок выполнения:** Строго по приоритетам — сначала все P0, потом все P1, потом P2, потом P3.

**Процесс для каждого TC:**
1. Написать failing test (Red)
2. Написать минимальный код для прохождения теста (Green)
3. Запустить все тесты — убедиться что ничего не сломалось
4. Рефакторинг если нужен
5. Commit с сообщением `Fix TC-XX: краткое описание`

### P0 (Critical)

#### TC‑01: Success Rate Race Condition

**Проблема:** Success rate показывает неправильное значение из-за race condition между remember() и cache.

**Решение:**
- Success rate **всегда** берётся из `CachedActivityData` вместе с `weekData` и `configTimeline`
- Расчёт success rate происходит в EffectHandler при вычислении activity data
- UI **никогда** не вычисляет success rate самостоятельно

**Тест:**
```kotlin
@Test
fun `success rate should be 100% after adding 9th habit and tracking all`() {
  // Given: 8 habits, all tracked for 7 days
  val state = HabitsState()
  val memos = buildList {
    add(habitConfigMemo(habits = listOf("Gym", "Study", "Meditation", ..., "Reading"))) // 8
    repeat(7) { day -> add(dailyMemo(day, allHabits = true)) }
  }

  // When: add 9th habit and track it
  val newMemos = memos + habitConfigMemo(habits = listOf(..., "Chess")) // 9
  newMemos.addAll(/* track Chess for 7 days */)

  // Trigger recalculation
  val effect = HabitsEffect.RecalculateActivityData(
    range = thisWeek,
    mode = ActivityMode.HABITS,
    appMode = AppMode.DEMO,
    memos = newMemos,
  )
  val actions = effectHandler.handle(effect, state).toList()

  // Then: UpdateActivityData should contain 100% success rate
  val updateAction = actions.filterIsInstance<HabitsAction.UpdateActivityData>().single()
  assertEquals(1.0f, updateAction.successRate?.rate)
}
```

**Изменения:**
- Добавить `successRate: SuccessRateData?` в `CachedActivityData`
- В `HabitsEffectHandler.recalculateActivityData()` вычислять success rate
- UI читает `habitsState.activityDataCache[key]?.successRate`

#### TC‑10: Settings Mode Switch Doesn't Refresh Data

**Проблема:** После смены режима (Demo ↔ Offline) данные не обновляются.

**Решение:**
- AppMode — часть ключей LaunchedEffect и ActivityCacheKey
- При смене режима → app shell отправляет `HabitsAction.InvalidateAllCache`
- LaunchedEffect с ключом `appMode` перезапускает prewarm

**Тест:**
```kotlin
@Test
fun `switching app mode should invalidate cache and trigger prewarm`() {
  // Given: Demo mode with cached data
  val appState = AppState(appMode = AppMode.DEMO)
  val habitsState = HabitsState(
    activityDataCache = mapOf(week1Key to demoData),
    isInitialLoading = false,
  )

  // When: switch to Offline mode
  val (newAppState, _) = appReducer.reduce(appState, AppAction.SwitchMode(AppMode.OFFLINE))
  // Then: app shell should dispatch HabitsAction.InvalidateAllCache (integration test)

  // And when action reduced
  val (newHabitsState, _) = habitsReducer.reduce(habitsState, HabitsAction.InvalidateAllCache)

  // Then: cache should be cleared and refresh flagged
  assertTrue(newHabitsState.activityDataCache.isEmpty())
  assertTrue(newHabitsState.needsCacheRefresh)
  assertTrue(newHabitsState.isInitialLoading)
}
```

**Изменения:**
- В HabitsReducer: обработать `InvalidateAllCache`
- В app shell (AppRoot/VibitsAppScaffold): при смене `appMode` отправить `HabitsAction.InvalidateAllCache`
- В UI: добавить `appMode` в ключи `LaunchedEffect` и `ActivityCacheKey`

### P1 (High)

#### TC‑03: Content Flashing / Black Screen on Week Switch

**Проблема:** При переключении между неделями показывается черный экран или empty state.

**Решение:**
- **Не очищать** кэш при инвалидации
- Использовать `isRecalculating` для пометки ключей
- Старые данные остаются видимыми пока идёт пересчёт

**Тест:**
```kotlin
@Test
fun `switching weeks should not clear existing cache`() {
  // Given: week 1 cached
  val appMode = AppMode.DEMO
  val week1Key = ActivityCacheKey(week1Range, ActivityMode.HABITS, appMode)
  val week2Key = ActivityCacheKey(week2Range, ActivityMode.HABITS, appMode)
  val state = HabitsState(
    activityDataCache = mapOf(week1Key to cachedData1),
  )

  // When: manually invalidate for week 2
  val (newState, effects) = reducer.reduce(
    state,
    HabitsAction.InvalidateCache(
      range = week2Range,
      mode = ActivityMode.HABITS,
      appMode = appMode,
      memos = memos,
    ),
  )

  // Then: week 1 data should still exist
  assertNotNull(newState.activityDataCache[week1Key])
  assertTrue(week2Key in newState.isRecalculating)
}
```

**Изменения:**
- В `HabitsReducer`: удалить любые `activityDataCache = emptyMap()`
- Использовать `isRecalculating + key` вместо очистки

#### TC‑06: UI Not Updating After Marking Habit as Done

**Проблема:** После marking habit кэш не инвалидируется, UI показывает старые данные.

**Решение:**
- При `MemoCreated/Updated/Deleted` выставить `needsCacheRefresh = true`
- LaunchedEffect увидит флаг и запустит prewarm

**Тест:**
```kotlin
@Test
fun `memo operations should set needsCacheRefresh flag`() {
  val state = HabitsState(needsCacheRefresh = false)

  val actions = listOf(
    HabitsAction.MemoCreated(testMemo),
    HabitsAction.MemoUpdated(testMemo),
    HabitsAction.MemoDeleted("test.md"),
  )

  actions.forEach { action ->
    val (newState, _) = reducer.reduce(state, action)
    assertTrue(newState.needsCacheRefresh, "Action $action should set needsCacheRefresh")
  }
}
```

**Изменения:**
- В `HabitsReducer`: при memo операциях добавить `needsCacheRefresh = true`
- Не эмитить effect напрямую — пусть UI LaunchedEffect обработает

#### TC‑07: Graph Disappearing After Update

**Проблема:** После update graph пропадает из-за очистки кэша.

**Решение:**
- Не очищать кэш при memo операциях
- UI логика: если `isRecalculating` и данные есть в кэше → показывать старые данные

**Тест:**
```kotlin
@Test
fun `memo update should not clear cache`() {
  // Given: cache with data
  val appMode = AppMode.DEMO
  val week1Key = ActivityCacheKey(week1Range, ActivityMode.HABITS, appMode)
  val week2Key = ActivityCacheKey(week2Range, ActivityMode.HABITS, appMode)
  val state = HabitsState(
    activityDataCache = mapOf(week1Key to cachedData1, week2Key to cachedData2),
  )

  // When: memo updated
  val (newState, _) = reducer.reduce(state, HabitsAction.MemoUpdated(testMemo))

  // Then: cache should not be cleared
  assertEquals(2, newState.activityDataCache.size)
  assertNotNull(newState.activityDataCache[week1Key])
  assertNotNull(newState.activityDataCache[week2Key])
  assertTrue(newState.needsCacheRefresh)
}
```

**Изменения:**
- В `HabitsReducer`: удалить `activityDataCache = emptyMap()` из memo операций
- В UI: проверять `habitsState.isDataLoading(key)` и показывать cached data

### P2 (Medium)

#### TC‑04: Infinite Loader on App Start

**Проблема:** Prewarm эмитит actions изнутри `withContext` → Flow invariant violation → loader не исчезает.

**Решение:**
- Собрать результаты внутри `withContext`, эмитить **после** выхода (единственный вариант)

**Тест:**
```kotlin
@Test
fun `prewarm should complete and emit PrewarmCompleted as last action`() = runTest {
  val effectHandler = HabitsEffectHandler(...)
  val memos = buildTestMemos()

  // When
  val actions = effectHandler.handle(
    HabitsEffect.RunPrewarmAllRanges(memos, appMode = AppMode.DEMO),
    HabitsState(),
  ).toList()

  // Then: should emit UpdateActivityData actions
  val updateActions = actions.filterIsInstance<HabitsAction.UpdateActivityData>()
  assertTrue(updateActions.isNotEmpty())

  // And: PrewarmCompleted should be the last action
  assertEquals(HabitsAction.PrewarmCompleted, actions.last())
}
```

**Изменения:**
```kotlin
// В HabitsEffectHandler:
is HabitsEffect.RunPrewarmAllRanges -> flow {
  val results = withContext(Dispatchers.Default) {
    // Вычисления в background
    calculateAllRanges(effect.memos)
  }
  // Эмитим ПОСЛЕ withContext
  results.forEach { result ->
    emit(
      HabitsAction.UpdateActivityData(
        range = result.range,
        mode = result.mode,
        appMode = result.appMode,
        weekData = result.weekData,
        configTimeline = result.configTimeline,
        successRate = result.successRate,
      ),
    )
  }
  emit(HabitsAction.PrewarmCompleted)
}
```

#### TC‑05: UI Flashing When Swiping Through Uncached Weeks

**Проблема:** Prewarm только текущей недели → при свайпе shimmer.

**Решение:**
- Prewarm **всех** weeks/months/quarters/years от earliest memo до today
- Вычислять диапазон на основе `EarliestMemoDateUseCase(memos, timeZone)`

**Тест:**
```kotlin
@Test
fun `prewarm should cover all ranges from earliest memo to today`() = runTest {
  val memos = buildMemos(
    firstDate = LocalDate(2026, 1, 1),  // 4 weeks ago
    lastDate = LocalDate(2026, 1, 28),  // today
  )

  // When
  val actions = effectHandler.handle(
    HabitsEffect.RunPrewarmAllRanges(memos, appMode = AppMode.DEMO),
    HabitsState(),
  ).toList()

  val updateActions = actions.filterIsInstance<HabitsAction.UpdateActivityData>()
  val weekKeys = updateActions
    .filter { it.mode == ActivityMode.HABITS }
    .map { it.range }
    .filterIsInstance<ActivityRange.Week>()

  // Then: should have all 5 weeks
  assertEquals(5, weekKeys.size)

  // Should include earliest week
  assertTrue(weekKeys.any { it.startDate == LocalDate(2026, 1, 6) })

  // Should include current week
  assertTrue(weekKeys.any { it.startDate == LocalDate(2026, 1, 27) })
}
```

**Изменения:**
- В `HabitsEffectHandler.prewarmAllRanges()`:
  - Вычислить earliest memo date
  - Сгенерировать все weeks от earliest до today
  - Аналогично для months, quarters, years
  - Для каждого диапазона вычислить activity data

#### TC‑08: Memos Tab Graphs Not Showing for Months/Years

**Проблема:** PostsScreen создаёт пустой `HabitsState()` вместо использования shared state.

**Решение:**
- PostsScreen принимает `habitsState` и `onHabitsAction` как параметры
- SwipeableContent передаёт shared state в PostsScreen

**Тест (Compose UI test):**
```kotlin
@Test
fun `PostsScreen should display data from shared habitsState`() {
  composeTestRule.setContent {
    val appMode = AppMode.DEMO
    val habitsState = HabitsState(
      activityDataCache = mapOf(
        ActivityCacheKey(monthRange, ActivityMode.POSTS, appMode) to testData,
      ),
    )

    PostsScreen(
      memos = testMemos,
      range = monthRange,
      appMode = appMode,
      demoMode = false,
      dateFormatter = testFormatter,
      habitsState = habitsState,
      onHabitsAction = {},
    )
  }

  // Then: data should be visible
  composeTestRule.onNodeWithText("5 posts").assertExists()
}
```

**Изменения:**
- В `PostsScreen`: добавить параметры `habitsState: HabitsState`, `onHabitsAction: (HabitsAction) -> Unit`
- Удалить `habitsState = HabitsState()` из дефолтов
- В `SwipeableContent`: передавать shared state

### P3 (Low)

#### TC‑02: Date Label Jumping Horizontally

**Проблема:** Лейбл даты прыгает из-за появления/исчезания success rate badge.

**Решение:**
- Зарезервировать фиксированный слот под badge только для HABITS mode

**Тест (Compose UI test):**
```kotlin
@Test
fun `date label position should not change when badge appears`() {
  composeTestRule.setContent {
    var showBadge by remember { mutableStateOf(true) }

    Column {
      Button(onClick = { showBadge = !showBadge }) { Text("Toggle") }
      TimeRangeNavigator(
        rangeLabel = "Dec 23 - 29",
        successRate = if (showBadge) 1.0f else null,
        // ...
      )
    }
  }

  val positionWith = composeTestRule.onNodeWithText("Dec 23 - 29")
    .fetchSemanticsNode().positionInRoot.x

  composeTestRule.onNodeWithText("Toggle").performClick()

  val positionWithout = composeTestRule.onNodeWithText("Dec 23 - 29")
    .fetchSemanticsNode().positionInRoot.x

  assertEquals(positionWith, positionWithout, absoluteTolerance = 1.dp.toPx())
}
```

**Изменения:**
- В `TimeRangeNavigator`: зарезервировать фиксированный слот под badge **только** в HABITS mode
  - Если `successRate == null`, показывать пустой `Box(Modifier.width(BADGE_WIDTH))`
  - Это гарантирует неизменное положение заголовка

#### TC‑09: Success Rate Badge Shows on Memos Tab

**Проблема:** Placeholder "—" показывается в POSTS mode где success rate не нужен.

**Решение:**
- Передавать `mode: ActivityMode` в TimeRangeNavigator
- Показывать badge только для `mode == ActivityMode.HABITS`

**Тест:**
```kotlin
@Test
fun `success rate badge should not show for POSTS mode`() {
  composeTestRule.setContent {
    TimeRangeNavigator(
      rangeLabel = "Jan 2026",
      successRate = null,
      mode = ActivityMode.POSTS, // явно указываем режим
      // ...
    )
  }

  // Then: no placeholder should exist
  composeTestRule.onNodeWithText("—").assertDoesNotExist()
}
```

**Изменения:**
- В `TimeRangeNavigator`: добавить параметр `mode: ActivityMode`
- Условие: `if (mode == ActivityMode.HABITS && successRate != null) { Badge }`

---

## 4) Интеграция UI (фаза UX‑stability)

### 4.1 Удаление Compose-кэшей

**Что удалить:**
- Все `remember { ... }` блоки вычисляющие **activity data / success rate**
- `rememberHabitsConfigTimeline()`
- `rememberActivityWeekData()`
- `rememberSuccessRate()`
- DI‑level `ActivityWeekDataCache`

**Что оставить:**
- `remember` для UI state (expanded/collapsed, scroll position, etc.)
- `remember` для форматирования (labels, colors, etc.)

### 4.2 Prewarm trigger в app shell (единственная точка)

```kotlin
@Composable
fun VibitsAppScaffold(...) {
  val appMode = appState.appMode
  val memos = memosState.memos
  val memosRevision = memosState.memosRevision

  LaunchedEffect(appMode, memosRevision, habitsState.needsCacheRefresh, habitsState.isInitialLoading) {
    val shouldPrewarm =
      !habitsState.isInitialLoading &&
        (habitsState.needsCacheRefresh || habitsState.activityDataCache.isEmpty())
    if (memos.isNotEmpty() && shouldPrewarm) {
      onHabitsAction(HabitsAction.RequestPrewarmAllRanges(memos = memos, appMode = appMode))
    }
  }
  // ... остальной UI
}
```

### 4.3 HabitsScreen read‑only (без prewarm)

```kotlin
@Composable
fun HabitsScreen(
  range: ActivityRange,
  appMode: AppMode,
  habitsState: HabitsState,
  onHabitsAction: (HabitsAction) -> Unit,
) {

  // UI reads ONLY from habitsState
  val key = ActivityCacheKey(range, ActivityMode.HABITS, appMode)
  val cachedData = habitsState.activityDataCache[key]
  val isLoading = habitsState.isDataLoading(key)

  when {
    cachedData != null -> {
      // Show data (even if isLoading — старые данные остаются)
      ActivityChart(
        weekData = cachedData.weekData,
        successRate = cachedData.successRate,
      )
    }
    isLoading -> {
      // Shimmer/skeleton
      CircularProgressIndicator()
    }
    else -> {
      // Empty state
      EmptyStateMessage()
    }
  }
}
```

### 4.4 PostsScreen Integration

```kotlin
@Composable
fun PostsScreen(
  memos: List<Memo>,
  range: ActivityRange,
  appMode: AppMode,
  habitsState: HabitsState,        // Shared state!
  onHabitsAction: (HabitsAction) -> Unit,  // Shared handler!
  dateFormatter: DateFormatter,
  demoMode: Boolean,
  // ... other params
) {
  // PostsScreen не запускает prewarm, это делает app shell

  StatsScreen(
    state = StatsScreenState(...),
    dateFormatter = dateFormatter,
    appMode = appMode,
    habitsState = habitsState,  // Pass shared state
    onHabitsAction = onHabitsAction,  // Pass shared handler
  )
}
```

**Изменения в SwipeableContent:**
```kotlin
PostsScreen(
  memos = memosState.memos,
  range = appState.currentActivityRange,
  appMode = appState.appMode,
  demoMode = appState.isDemoMode,
  dateFormatter = dateFormatter,
  habitsState = habitsState,        // Pass from parent
  onHabitsAction = onHabitsAction,  // Pass from parent
)
```

### 4.4 Helper для HabitsState

```kotlin
// В HabitsState.kt (extension):
fun HabitsState.isDataLoading(key: ActivityCacheKey): Boolean =
  key in isRecalculating || (isInitialLoading && key !in activityDataCache)

fun HabitsState.getActivityData(range: ActivityRange, mode: ActivityMode, appMode: AppMode): CachedActivityData? =
  activityDataCache[ActivityCacheKey(range, mode, appMode)]
```

### 4.5 Стратегия миграции (Transition)

**Вопрос:** Можно ли иметь старую и новую систему одновременно?
**Ответ:** Нет. Две системы кэширования создадут race conditions и несогласованность.

**Подход:** Big Bang Migration за один PR, но с тестами на каждом шаге.

**Процесс разработки:**
- Создать feature‑ветку, все изменения — через PR (не коммитить в `main`).

**Этапы миграции:**

1. **Подготовка (без ломки):**
   - Добавить новые поля в HabitsState (cache, flags)
   - Добавить новые Actions/Effects
   - Написать EffectHandler логику (пока не используется)
   - Написать тесты для новой логики
   - **Commit:** "Add TEA cache structures and logic (unused)"

2. **Переключение UI (breaking change):**
   - В HabitsScreen: заменить `remember` блоки на чтение из `habitsState.activityDataCache`
   - Добавить prewarm‑trigger в app shell (одна точка)
   - **ВАЖНО:** В этом коммите старая система перестаёт работать
   - Запустить приложение, проверить что всё работает
   - **Commit:** "Switch HabitsScreen to TEA cache"

3. **Удаление старой системы:**
   - Удалить `rememberActivityWeekData()` и другие remember-функции
   - Удалить `ActivityWeekDataCache`
   - Удалить неиспользуемый код
   - **Commit:** "Remove old Compose-layer cache"

4. **Интеграция PostsScreen:**
   - Обновить PostsScreen для shared state
   - Обновить SwipeableContent
   - **Commit:** "Integrate PostsScreen with shared cache"

5. **UI polish:**
   - Исправить TC-02 (date jump)
   - Исправить TC-09 (badge in posts)
   - **Commits:** По одному на каждый TC

**Откат в случае проблем:**
- Git revert коммита "Switch HabitsScreen to TEA cache"
- Старая система работает как раньше

---

## 5) Детали реализации

### 5.1 Где хранить CachedActivityData и ActivityCacheKey?

**Вопрос:** В какой package положить эти классы?

**Ответ:**
```
feature/habits/
  presentation/
    ActivityCacheKey.kt       # Presentation model (TEA cache key)
    CachedActivityData.kt     # Presentation model (TEA cache entry)
    HabitsState.kt            # Использует модели кэша
```

**Обоснование:**
- Ключ и данные кэша — часть TEA state (presentation слой), не доменная модель
- Домен остаётся чистым (use cases принимают доменные структуры)

### 5.2 Как вычислять Success Rate?

**Место:** Use case или EffectHandler?

**Решение:** Use case для переиспользования и тестируемости.

```kotlin
// Используем существующий CalculateSuccessRateUseCase из проекта
val successRate = if (mode == ActivityMode.HABITS && configTimeline.isNotEmpty()) {
  calculateSuccessRate(weekData, range, today, configStartDate)
} else {
  null
}
```

**Использование в EffectHandler:**
```kotlin
private suspend fun calculateActivityData(
  range: ActivityRange,
  mode: ActivityMode,
  appMode: AppMode,
  memos: List<Memo>,
): CachedActivityData {
  val timeZone = TimeZone.currentSystemDefault()
  val today = currentLocalDate()
  val configTimeline = ExtractHabitsConfigUseCase(memos, timeZone)
  val dailyMemos = ExtractDailyMemosUseCase(memos, timeZone)
  val weekData = buildActivityDataUseCase.buildWeekData(
    configTimeline = if (mode == ActivityMode.HABITS) configTimeline else emptyList(),
    dailyMemos = dailyMemos,
    timeZone = timeZone,
    memos = memos,
    range = range,
    mode = mode,
    today = today,
  )
  val configStartDate = configTimeline.firstOrNull()?.date
  val successRate = if (mode == ActivityMode.HABITS && configTimeline.isNotEmpty()) {
    calculateSuccessRate(weekData, range, today, configStartDate)
  } else {
    null
  }

  return CachedActivityData(
    weekData = weekData,
    configTimeline = configTimeline,
    successRate = successRate,
  )
}
```

### 5.3 Prewarm: какие диапазоны?

**Для HABITS mode:**
- Все weeks от earliest memo до today
- Все months от earliest memo до today
- Все quarters от earliest memo до today
- Все years от earliest memo до today

**Для POSTS mode:**
- То же самое (данные могут отличаться, но диапазоны те же)

**Детальная реализация:**
```kotlin
private suspend fun prewarmAllRanges(memos: List<Memo>, appMode: AppMode): List<PrewarmResult> {
  val timeZone = TimeZone.currentSystemDefault()
  val earliestDate = EarliestMemoDateUseCase(memos, timeZone) ?: return emptyList()
  val today = currentLocalDate()

  val ranges = buildList {
    addAll(generateWeeks(earliestDate, today))
    addAll(generateMonths(earliestDate, today))
    addAll(generateQuarters(earliestDate, today))
    addAll(generateYears(earliestDate, today))
  }

  val modes = listOf(ActivityMode.HABITS, ActivityMode.POSTS)

  return withContext(Dispatchers.Default) {
    ranges.flatMap { range ->
      modes.map { mode ->
        async {
          val data = calculateActivityData(range, mode, appMode, memos)
          PrewarmResult(range, mode, appMode, data.weekData, data.configTimeline, data.successRate)
        }
      }
    }.awaitAll()
  }
}

// Helper функции для генерации диапазонов
private fun generateWeeks(startDate: LocalDate, endDate: LocalDate): List<ActivityRange.Week> {
  val weeks = mutableListOf<ActivityRange.Week>()
  var cursor = startOfWeek(startDate) // Использовать существующую функцию из DateUtils
  while (cursor <= endDate) {
    weeks.add(ActivityRange.Week(cursor))
    cursor = cursor.plus(DatePeriod(days = 7))
  }
  return weeks
}

private fun generateMonths(startDate: LocalDate, endDate: LocalDate): List<ActivityRange.Month> {
  val months = mutableListOf<ActivityRange.Month>()
  var cursor = ActivityRange.Month(startDate.year, startDate.month)
  val end = ActivityRange.Month(endDate.year, endDate.month)
  while (cursor.year < end.year || (cursor.year == end.year && cursor.month <= end.month)) {
    months.add(cursor)
    val nextDate = LocalDate(cursor.year, cursor.month, 1).plus(DatePeriod(months = 1))
    cursor = ActivityRange.Month(nextDate.year, nextDate.month)
  }
  return months
}

private fun generateQuarters(startDate: LocalDate, endDate: LocalDate): List<ActivityRange.Quarter> {
  val quarters = mutableListOf<ActivityRange.Quarter>()
  var cursor = ActivityRange.Quarter(startDate.year, quarterIndex(startDate))
  val end = ActivityRange.Quarter(endDate.year, quarterIndex(endDate))
  while (cursor.year < end.year || (cursor.year == end.year && cursor.index <= end.index)) {
    quarters.add(cursor)
    cursor = NavigateActivityRangeUseCase(cursor, 1) as ActivityRange.Quarter
  }
  return quarters
}

private fun generateYears(startDate: LocalDate, endDate: LocalDate): List<ActivityRange.Year> =
  (startDate.year..endDate.year).map { ActivityRange.Year(it) }

data class PrewarmResult(
  val range: ActivityRange,
  val mode: ActivityMode,
  val appMode: AppMode,
  val weekData: ActivityWeekData,
  val configTimeline: List<HabitsConfigEntry>,
  val successRate: SuccessRateData?,
)
```

### 5.4 Обработка RequestPrewarmAllRanges в Reducer

```kotlin
is HabitsAction.RequestPrewarmAllRanges -> {
  state {
    copy(
      needsCacheRefresh = false,  // Сбрасываем флаг
      isInitialLoading = true,    // Помечаем что идёт загрузка
    )
  }
  effect(HabitsEffect.RunPrewarmAllRanges(action.memos, action.appMode))
}

is HabitsAction.UpdateActivityData -> {
  val key = ActivityCacheKey(action.range, action.mode, action.appMode)
  state {
    copy(
      activityDataCache =
        activityDataCache +
          (key to CachedActivityData(action.weekData, action.configTimeline, action.successRate)),
      isRecalculating = isRecalculating - key,
    )
  }
}

is HabitsAction.PrewarmCompleted -> {
  state {
    copy(
      isInitialLoading = false,
    )
  }
}
```

---

## 6) Инструменты верификации

**После каждого TC:**
- Запуск релевантных unit/compose тестов.
- Отдельный коммит с коротким сообщением.

**После всех TC:**
- `./gradlew :shared:desktopTest`
- При необходимости: `./gradlew :shared:koverHtmlReport`

---

## 7) Risk‑чеклист (как не повторить ошибки)

**Перед началом работы:**
- [ ] Прочитать весь план и все TC из REFACTORING_TEA_CACHE.md
- [ ] Убедиться что понял все структуры данных и их назначение
- [ ] Подготовить окружение: `./gradlew installGitHooks`

**Во время реализации:**
- [ ] Вся логика расчётов живёт в TEA (EffectHandler/UseCase), UI не делает бизнес‑логики
- [ ] UI слой **read-only** — только читает из state, никаких вычислений
- [ ] Нет полного `clear()` кэша при пересчётах (кроме AppMode switch)
- [ ] `InvalidateCache` не удаляет данные, только помечает через `isRecalculating`
- [ ] `Prewarm` не эмитит из background dispatcher напрямую (emit после withContext)
- [ ] Триггеры LaunchedEffect учитывают **appMode** и `memosRevision`
- [ ] `ActivityCacheKey` включает `appMode`
- [ ] Success rate вычисляется в EffectHandler, хранится в CachedActivityData
- [ ] Success rate показывается **только** для ActivityMode.HABITS
- [ ] `PostsScreen` использует shared `HabitsState` (не создаёт новый)
- [ ] Заголовок даты не прыгает (зарезервировано место под badge)
- [ ] После каждого TC запускать тесты и проверять что ничего не сломалось
- [ ] Каждый TC — отдельный коммит

**После завершения:**
- [ ] Все TC‑01..TC‑10 тесты зелёные
- [ ] `./gradlew :shared:desktopTest` проходит
- [ ] Coverage не упал: `./gradlew :shared:koverHtmlReport`
- [ ] Удалён весь старый код (remember-блоки, DI cache)
- [ ] Запустить приложение вручную и проверить основные сценарии:
  - [ ] Добавление 9-го хабита → success rate 100%
  - [ ] Marking habit as done → UI обновляется мгновенно
  - [ ] Переключение недель → нет мигания
  - [ ] Смена режима Demo ↔ Offline → данные обновляются
  - [ ] Memos tab → графики показываются для months/years
  - [ ] Success rate badge НЕ показывается на Memos tab

---

## 8) Итог

Рефакторинг выполняется шаг‑за‑шагом по приоритетам (P0 → P1 → P2 → P3). Каждый баг переводится в failing test, затем фиксится минимальным кодом, затем коммитится.

**Результат после завершения:**
- Все TC‑01..TC‑10 тесты зелёные
- Вся бизнес-логика в TEA layer (HabitsState + EffectHandler)
- UI слой read-only, без кэшей и вычислений
- Нет race conditions (единый источник истины)
- Нет flashing/blinking при навигации
- Success rate всегда консистентен с данными
- Смена режима корректно обновляет данные
- PostsScreen использует shared state
- Coverage не упал

**Ожидаемый объём работы:**
- ~10 новых Actions/Effects
- ~5 новых полей в HabitsState
- ~3 новых use cases
- ~15 unit tests
- ~3 Compose UI tests
- ~500-800 строк нового кода
- ~300-500 строк удалённого старого кода

---

## 9) FAQ (Часто задаваемые вопросы)

### Q: Почему нельзя очищать кэш при пересчёте?
**A:** Очистка кэша → UI читает null → показывает empty state → пользователь видит мигание. Вместо этого оставляем старые данные видимыми пока вычисляются новые.

### Q: Когда можно полностью очистить кэш?
**A:** Только при смене AppMode (Demo ↔ Offline), т.к. это смена источника данных и старые данные больше не актуальны.

### Q: Зачем нужен флаг needsCacheRefresh?
**A:** Чтобы LaunchedEffect знал что нужно перезапустить prewarm. Без флага LaunchedEffect не знает что мемо изменился.

### Q: Почему используем memosRevision вместо простого memos.hashCode()?
**A:** memosRevision — явный контракт между MemosState и HabitsState. Hash может коллизировать, revision всегда монотонно растёт.

### Q: Почему isInitialLoading = false по умолчанию?
**A:** Prewarm запускается при пустом кэше через `shouldPrewarm`. Флаг `isInitialLoading` включается только на время фактического prewarm, чтобы не ловить бесконечный loader при пустых memos.

### Q: Где вычислять success rate — в Reducer или EffectHandler?
**A:** В EffectHandler (через @Inject CalculateSuccessRateUseCase). Reducer должен быть чистым и быстрым, тяжёлые вычисления — в effects.

### Q: Почему generateWeeks/Months не реализованы как отдельные use cases?
**A:** Это простые utility функции без бизнес-логики. Достаточно private функций в EffectHandler.

### Q: Как app shell узнаёт об смене AppMode?
**A:** Через LaunchedEffect(appState.appMode) в главном Composable. При изменении appMode dispatch HabitsAction.InvalidateAllCache.

### Q: Откуда брать существующие use cases и функции?
**A:** Все нужные use cases уже существуют:
- `CalculateSuccessRateUseCase` — `@Inject class` в `habits/domain/usecase/`
- `BuildActivityDataUseCase` — `@Inject class` с методом `buildWeekData()`
- `EarliestMemoDateUseCase` — `object` в `habits/domain/usecase/`
- `ExtractHabitsConfigUseCase` — в `habits/domain/usecase/`
- `ExtractDailyMemosUseCase` — в `habits/domain/usecase/`
- `currentLocalDate()` — `expect fun` в `core/platform/date/DateProvider.kt`
- `startOfWeek()` — в `habits/domain/usecase/DateUtils.kt`

### Q: Как PostsScreen получает HabitsState?
**A:** Через параметры. SwipeableContent (или родительский composable) передаёт shared state в оба экрана.

### Q: Нужно ли кэшировать данные на диск?
**A:** Нет. В этом рефакторинге используем только in-memory кэш.

### Q: Что делать если тест упал после фикса другого TC?
**A:** Откатить изменения, понять root cause, исправить правильно. Не коммитить пока все тесты не зелёные.

### Q: Сколько времени займёт рефакторинг?
**A:** [Не даём оценок времени согласно инструкциям]. Фокус на качестве, не на скорости. TDD гарантирует корректность.

### Q: Можно ли делать несколько TC в одном коммите?
**A:** Можно, если они тесно связаны (например, TC-03 и TC-07 оба про "не очищать кэш"). Но лучше разделять для clarity.

### Q: Что делать с существующими remember-блоками во время миграции?
**A:** Удалить их в коммите "Switch to TEA cache". Не оставлять dead code.

**Дата создания:** 28 января 2026
**Последнее обновление:** 28 января 2026
**Статус:** План готов к исполнению
**Ревью:** Все критические моменты проверены, открытых вопросов нет

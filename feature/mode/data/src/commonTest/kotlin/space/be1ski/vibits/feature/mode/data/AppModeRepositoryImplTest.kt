package space.be1ski.vibits.feature.mode.data

import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.data.test.FakeAppModeStore
import kotlin.test.Test
import kotlin.test.assertEquals

class AppModeRepositoryImplTest {
  @Test
  fun `when loadMode then returns mode from store`() {
    val store = FakeAppModeStore(initial = LocalAppMode(mode = AppMode.ONLINE))
    val repository = AppModeRepositoryImpl(store)

    val result = repository.loadMode()

    assertEquals(AppMode.ONLINE, result)
  }

  @Test
  fun `when loadMode with demo mode then returns demo`() {
    val store = FakeAppModeStore(initial = LocalAppMode(mode = AppMode.DEMO))
    val repository = AppModeRepositoryImpl(store)

    val result = repository.loadMode()

    assertEquals(AppMode.DEMO, result)
  }

  @Test
  fun `when loadMode with offline mode then returns offline`() {
    val store = FakeAppModeStore(initial = LocalAppMode(mode = AppMode.OFFLINE))
    val repository = AppModeRepositoryImpl(store)

    val result = repository.loadMode()

    assertEquals(AppMode.OFFLINE, result)
  }

  @Test
  fun `when loadMode with not selected then returns not selected`() {
    val store = FakeAppModeStore(initial = LocalAppMode(mode = AppMode.NOT_SELECTED))
    val repository = AppModeRepositoryImpl(store)

    val result = repository.loadMode()

    assertEquals(AppMode.NOT_SELECTED, result)
  }

  @Test
  fun `when saveMode then persists mode to store`() {
    val store = FakeAppModeStore()
    val repository = AppModeRepositoryImpl(store)

    repository.saveMode(AppMode.ONLINE)

    assertEquals(LocalAppMode(mode = AppMode.ONLINE), store.stored)
    assertEquals(1, store.saveCalls)
  }

  @Test
  fun `when saveMode with different modes then persists each correctly`() {
    val store = FakeAppModeStore()
    val repository = AppModeRepositoryImpl(store)

    repository.saveMode(AppMode.DEMO)
    assertEquals(LocalAppMode(mode = AppMode.DEMO), store.stored)

    repository.saveMode(AppMode.OFFLINE)
    assertEquals(LocalAppMode(mode = AppMode.OFFLINE), store.stored)

    repository.saveMode(AppMode.NOT_SELECTED)
    assertEquals(LocalAppMode(mode = AppMode.NOT_SELECTED), store.stored)

    assertEquals(3, store.saveCalls)
  }
}

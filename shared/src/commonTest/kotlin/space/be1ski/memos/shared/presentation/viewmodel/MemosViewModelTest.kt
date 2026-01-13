package space.be1ski.memos.shared.presentation.viewmodel

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import space.be1ski.memos.shared.data.local.StorageInfoProvider
import space.be1ski.memos.shared.domain.model.auth.Credentials
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.domain.usecase.CreateMemoUseCase
import space.be1ski.memos.shared.domain.usecase.DeleteMemoUseCase
import space.be1ski.memos.shared.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.memos.shared.domain.usecase.LoadCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.LoadMemosUseCase
import space.be1ski.memos.shared.domain.usecase.LoadStorageInfoUseCase
import space.be1ski.memos.shared.domain.usecase.SaveCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.UpdateMemoUseCase
import space.be1ski.memos.shared.presentation.state.MemosUiState
import space.be1ski.memos.shared.test.FakeCredentialsRepository
import space.be1ski.memos.shared.test.FakeMemosRepository

@OptIn(ExperimentalCoroutinesApi::class)
class MemosViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `when credentials missing then initial state asks for input`() = runTest {
    val viewModel = buildViewModel(Credentials(baseUrl = "", token = ""))

    assertIs<MemosUiState.CredentialsInput>(viewModel.uiState)
  }

  @Test
  fun `when cached memos exist then preload shows them`() = runTest {
    val cached = listOf(
      Memo(name = "memos/1", content = "Cached", createTime = Instant.parse("2024-01-01T00:00:00Z"))
    )
    val viewModel = buildViewModel(
      credentials = Credentials(baseUrl = "https://example.com", token = "token"),
      cachedMemos = cached
    )

    advanceUntilIdle()

    val state = viewModel.uiState
    assertIs<MemosUiState.Ready>(state)
    assertEquals(cached, state.memos)
  }

  @Test
  fun `when loadMemos with blank credentials then shows error`() = runTest {
    val viewModel = buildViewModel(Credentials(baseUrl = "", token = ""))

    viewModel.loadMemos()

    val state = viewModel.uiState
    assertIs<MemosUiState.CredentialsInput>(state)
    assertEquals("Base URL and token are required.", state.errorMessage)
  }

  @Test
  fun `when loadMemos succeeds then updates state and saves credentials`() = runTest {
    val repository = FakeMemosRepository()
    val credentialsRepository = FakeCredentialsRepository(Credentials(baseUrl = "", token = ""))
    repository.cachedMemosResult = listOf(
      Memo(name = "memos/1", content = "Cached", createTime = Instant.parse("2024-01-01T00:00:00Z"))
    )
    repository.listMemosResult = Result.success(
      listOf(
        Memo(name = "memos/1", content = "Old", createTime = Instant.parse("2024-01-01T00:00:00Z")),
        Memo(name = "memos/2", content = "New", updateTime = Instant.parse("2024-02-01T00:00:00Z"))
      )
    )
    val viewModel = buildViewModel(
      credentials = credentialsRepository.stored,
      memosRepository = repository,
      credentialsRepository = credentialsRepository
    )

    viewModel.updateBaseUrl("https://example.com")
    viewModel.updateToken("token")
    viewModel.loadMemos()
    advanceUntilIdle()

    val state = viewModel.uiState
    assertIs<MemosUiState.Ready>(state)
    assertEquals(2, state.memos.size)
    assertEquals("memos/2", state.memos.first().name)
    assertEquals(1, credentialsRepository.saveCount)
    assertFalse(state.isLoading)
  }

  @Test
  fun `when loadMemos fails then keeps error`() = runTest {
    val repository = FakeMemosRepository()
    repository.listMemosResult = Result.failure(IllegalStateException("Boom"))
    val viewModel = buildViewModel(
      credentials = Credentials(baseUrl = "https://example.com", token = "token"),
      memosRepository = repository
    )

    viewModel.loadMemos()
    advanceUntilIdle()

    val state = viewModel.uiState
    assertIs<MemosUiState.Ready>(state)
    assertEquals("Boom", state.errorMessage)
    assertFalse(state.isLoading)
  }

  @Test
  fun `when update create delete then state reflects changes`() = runTest {
    val repository = FakeMemosRepository()
    repository.listMemosResult = Result.success(
      listOf(Memo(name = "memos/1", content = "Old", createTime = Instant.parse("2024-01-01T00:00:00Z")))
    )
    repository.updateMemoResult = Result.success(
      Memo(name = "memos/1", content = "Updated", updateTime = Instant.parse("2024-02-01T00:00:00Z"))
    )
    repository.createMemoResult = Result.success(
      Memo(name = "memos/2", content = "Created", createTime = Instant.parse("2024-03-01T00:00:00Z"))
    )
    val viewModel = buildViewModel(
      credentials = Credentials(baseUrl = "https://example.com", token = "token"),
      memosRepository = repository
    )

    viewModel.loadMemos()
    advanceUntilIdle()

    viewModel.updateMemo("memos/1", "Updated")
    advanceUntilIdle()
    assertTrue(viewModel.uiState.memos.any { it.content == "Updated" })

    viewModel.createMemo("Created")
    advanceUntilIdle()
    assertTrue(viewModel.uiState.memos.any { it.name == "memos/2" })

    viewModel.deleteDailyMemo("memos/1")
    advanceUntilIdle()
    assertFalse(viewModel.uiState.memos.any { it.name == "memos/1" })
  }

  @Test
  fun `when update memo fails then shows error`() = runTest {
    val repository = FakeMemosRepository()
    repository.listMemosResult = Result.success(emptyList())
    repository.updateMemoResult = Result.failure(IllegalStateException("Update failed"))
    val viewModel = buildViewModel(
      credentials = Credentials(baseUrl = "https://example.com", token = "token"),
      memosRepository = repository
    )

    viewModel.loadMemos()
    advanceUntilIdle()

    viewModel.updateMemo("memos/1", "Updated")
    advanceUntilIdle()

    val state = viewModel.uiState
    assertEquals("Update failed", state.errorMessage)
    assertFalse(state.isLoading)
  }

  @Test
  fun `when editCredentials then populates stored values`() = runTest {
    val repository = FakeMemosRepository()
    repository.listMemosResult = Result.success(
      listOf(Memo(name = "memos/1", content = "Existing"))
    )
    val credentials = Credentials(baseUrl = "https://example.com", token = "token")
    val viewModel = buildViewModel(
      credentials = credentials,
      memosRepository = repository,
      credentialsRepository = FakeCredentialsRepository(credentials)
    )

    viewModel.loadMemos()
    advanceUntilIdle()

    viewModel.editCredentials()

    val state = viewModel.uiState
    assertIs<MemosUiState.CredentialsInput>(state)
    assertEquals("https://example.com", state.baseUrl)
    assertEquals("token", state.token)
    assertEquals(1, state.memos.size)
  }

  private fun buildViewModel(
    credentials: Credentials,
    cachedMemos: List<Memo> = emptyList(),
    memosRepository: FakeMemosRepository = FakeMemosRepository(),
    credentialsRepository: FakeCredentialsRepository = FakeCredentialsRepository(credentials)
  ): MemosViewModel {
    memosRepository.cachedMemosResult = cachedMemos
    val loadMemosUseCase = LoadMemosUseCase(memosRepository)
    val loadCachedMemosUseCase = LoadCachedMemosUseCase(memosRepository)
    val loadCredentialsUseCase = LoadCredentialsUseCase(credentialsRepository)
    val loadStorageInfoUseCase = LoadStorageInfoUseCase(StorageInfoProvider())
    val saveCredentialsUseCase = SaveCredentialsUseCase(credentialsRepository)
    val updateMemoUseCase = UpdateMemoUseCase(memosRepository)
    val createMemoUseCase = CreateMemoUseCase(memosRepository)
    val deleteMemoUseCase = DeleteMemoUseCase(memosRepository)

    return MemosViewModel(
      loadMemosUseCase,
      loadCachedMemosUseCase,
      loadCredentialsUseCase,
      loadStorageInfoUseCase,
      saveCredentialsUseCase,
      updateMemoUseCase,
      createMemoUseCase,
      deleteMemoUseCase
    )
  }
}

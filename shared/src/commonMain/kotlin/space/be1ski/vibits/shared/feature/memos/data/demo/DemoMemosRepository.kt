package space.be1ski.vibits.shared.feature.memos.data.demo

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "DemoMemosRepo"

@Inject
@SingleIn(AppScope::class)
class DemoMemosRepository : MemosRepository {
  private val memos = mutableListOf<Memo>()
  private var initialized = false

  fun reset() {
    Log.d(TAG, "Resetting demo memos repository")
    memos.clear()
    memos.addAll(DemoDataGenerator.generateDemoMemos())
    initialized = true
    Log.d(TAG, "Reset complete, now have ${memos.size} memos")
  }

  private fun ensureInitialized() {
    if (!initialized) {
      reset()
    }
  }

  override suspend fun listMemos(): List<Memo> {
    ensureInitialized()
    Log.d(TAG, "listMemos() returning ${memos.size} memos")
    return memos.toList()
  }

  override suspend fun cachedMemos(): List<Memo> {
    ensureInitialized()
    return memos.toList()
  }

  override suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo {
    ensureInitialized()
    Log.d(TAG, "updateMemo() called for: $name")
    Log.d(TAG, "New content:\n$content")
    val now = Clock.System.now()
    val index = memos.indexOfFirst { it.name == name }
    return if (index >= 0) {
      Log.d(TAG, "Updating existing memo at index $index")
      val updated = memos[index].copy(content = content, updateTime = now)
      memos[index] = updated
      Log.d(TAG, "After update, have ${memos.size} memos")
      updated
    } else {
      Log.d(TAG, "Creating new memo (name not found)")
      val memo = Memo(name = name, content = content, createTime = now, updateTime = now)
      memos.add(0, memo)
      Log.d(TAG, "After create, have ${memos.size} memos")
      memo
    }
  }

  @OptIn(ExperimentalUuidApi::class)
  override suspend fun createMemo(content: String): Memo {
    ensureInitialized()
    val now = Clock.System.now()
    val name = "memos/demo_${now.toEpochMilliseconds()}_${Uuid.random()}"
    val memo =
      Memo(
        name = name,
        content = content,
        createTime = now,
        updateTime = now,
      )
    memos.add(0, memo)
    return memo
  }

  override suspend fun deleteMemo(name: String) {
    ensureInitialized()
    memos.removeAll { it.name == name }
  }
}

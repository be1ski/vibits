package space.be1ski.vibits.shared.feature.memos.data.demo

import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

/**
 * In-memory repository for demo mode.
 * All changes are stored in memory and reset when demo mode is toggled.
 */
class DemoMemosRepository : MemosRepository {

  private val memos = mutableListOf<Memo>()

  init {
    reset()
  }

  /**
   * Resets the repository to initial demo data.
   * Called when entering demo mode to ensure fresh data.
   */
  fun reset() {
    memos.clear()
    memos.addAll(DemoDataGenerator.generateDemoMemos())
  }

  override suspend fun listMemos(): List<Memo> {
    return memos.toList()
  }

  override suspend fun cachedMemos(): List<Memo> {
    return memos.toList()
  }

  override suspend fun updateMemo(name: String, content: String): Memo {
    val now = Clock.System.now()
    val index = memos.indexOfFirst { it.name == name }
    return if (index >= 0) {
      val updated = memos[index].copy(content = content, updateTime = now)
      memos[index] = updated
      updated
    } else {
      Memo(name = name, content = content, updateTime = now)
    }
  }

  @OptIn(ExperimentalUuidApi::class)
  override suspend fun createMemo(content: String): Memo {
    val now = Clock.System.now()
    val name = "memos/demo_${now.toEpochMilliseconds()}_${Uuid.random()}"
    val memo = Memo(
      name = name,
      content = content,
      createTime = now,
      updateTime = now
    )
    memos.add(0, memo)
    return memo
  }

  override suspend fun deleteMemo(name: String) {
    memos.removeAll { it.name == name }
  }
}

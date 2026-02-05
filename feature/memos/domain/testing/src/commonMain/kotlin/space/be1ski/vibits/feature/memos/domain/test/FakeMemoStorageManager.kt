package space.be1ski.vibits.feature.memos.domain.test

import space.be1ski.vibits.feature.memos.domain.repository.MemoStorageManager

class FakeMemoStorageManager : MemoStorageManager {
  var clearAllCalls: Int = 0
    private set

  override fun clearAll() {
    clearAllCalls += 1
  }
}

package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.data.local.StorageInfoProvider
import space.be1ski.memos.shared.domain.model.storage.StorageInfo

/**
 * Loads storage details for diagnostics.
 */
class LoadStorageInfoUseCase(
  private val storageInfoProvider: StorageInfoProvider
) {
  /**
   * Returns storage locations and environment info.
   */
  operator fun invoke(): StorageInfo = storageInfoProvider.load()
}

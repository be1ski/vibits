package space.be1ski.memos.shared.config

import android.content.Context

object AndroidContextHolder {
  lateinit var context: Context
    private set

  fun set(context: Context) {
    this.context = context.applicationContext
  }

  fun isReady(): Boolean = ::context.isInitialized
}

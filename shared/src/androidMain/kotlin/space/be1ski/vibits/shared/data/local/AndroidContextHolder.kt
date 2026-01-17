package space.be1ski.vibits.shared.data.local

import android.annotation.SuppressLint
import android.content.Context

/**
 * Holds application context for shared Android code.
 */
@SuppressLint("StaticFieldLeak")
object AndroidContextHolder {
  lateinit var context: Context
    private set

  /**
   * Stores the application context.
   */
  fun set(context: Context) {
    this.context = context.applicationContext
  }

  /**
   * Returns true if the context has been initialized.
   */
  fun isReady(): Boolean = ::context.isInitialized
}

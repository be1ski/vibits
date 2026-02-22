package space.be1ski.vibits.core.platform.network

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import javax.net.SocketFactory

/**
 * A [SocketFactory] that binds sockets to a physical network interface (e.g., WiFi)
 * when connecting to local/private network addresses.
 *
 * On macOS, when the app runs as a .app bundle, VPN Network Extensions can intercept
 * Java's BSD socket traffic, causing [java.net.NoRouteToHostException] for local addresses.
 * Binding to the physical interface bypasses VPN routing for local connections only.
 *
 * Connections to public/external addresses use default routing (unaffected by this factory).
 */
internal class PhysicalNetworkSocketFactory private constructor(
  private val localAddress: InetAddress,
) : SocketFactory() {
  override fun createSocket(): Socket = Socket().apply { bind(InetSocketAddress(localAddress, 0)) }

  override fun createSocket(
    host: String,
    port: Int,
  ): Socket = createBoundSocket(InetAddress.getByName(host), port)

  override fun createSocket(
    host: InetAddress,
    port: Int,
  ): Socket = createBoundSocket(host, port)

  override fun createSocket(
    host: String,
    port: Int,
    localHost: InetAddress,
    localPort: Int,
  ): Socket = Socket(host, port, localHost, localPort)

  override fun createSocket(
    address: InetAddress,
    port: Int,
    localAddress: InetAddress,
    localPort: Int,
  ): Socket = Socket(address, port, localAddress, localPort)

  private fun createBoundSocket(
    address: InetAddress,
    port: Int,
  ): Socket {
    val socket = Socket()
    if (address.isPrivateNetwork()) {
      socket.bind(InetSocketAddress(localAddress, 0))
    }
    socket.connect(InetSocketAddress(address, port))
    return socket
  }

  companion object {
    fun create(): SocketFactory? {
      if (!isMacOs()) return null
      return findPhysicalInterfaceAddress()?.let(::PhysicalNetworkSocketFactory)
    }

    private fun isMacOs(): Boolean = System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true

    private fun findPhysicalInterfaceAddress(): InetAddress? =
      try {
        NetworkInterface
          .getNetworkInterfaces()
          ?.toList()
          .orEmpty()
          .filter { it.isUp && !it.isLoopback && !it.isPointToPoint }
          .flatMap { it.inetAddresses.toList() }
          .firstOrNull { it is java.net.Inet4Address && !it.isLoopbackAddress }
      } catch (_: IOException) {
        null
      }
  }
}

private const val IPV4_BYTES = 4
private const val OCTET_MASK = 0xFF
private const val PRIVATE_CLASS_A = 10
private const val PRIVATE_CLASS_B_FIRST = 172
private const val PRIVATE_CLASS_B_SECOND_MIN = 16
private const val PRIVATE_CLASS_B_SECOND_MAX = 31
private const val PRIVATE_CLASS_C_FIRST = 192
private const val PRIVATE_CLASS_C_SECOND = 168
private const val LINK_LOCAL_FIRST = 169
private const val LINK_LOCAL_SECOND = 254

private fun InetAddress.isPrivateNetwork(): Boolean {
  val bytes = address
  if (bytes.size != IPV4_BYTES) return false
  val b0 = bytes[0].toInt() and OCTET_MASK
  val b1 = bytes[1].toInt() and OCTET_MASK
  return when {
    b0 == PRIVATE_CLASS_A -> true
    b0 == PRIVATE_CLASS_B_FIRST && b1 in PRIVATE_CLASS_B_SECOND_MIN..PRIVATE_CLASS_B_SECOND_MAX -> true
    b0 == PRIVATE_CLASS_C_FIRST && b1 == PRIVATE_CLASS_C_SECOND -> true
    b0 == LINK_LOCAL_FIRST && b1 == LINK_LOCAL_SECOND -> true
    else -> false
  }
}

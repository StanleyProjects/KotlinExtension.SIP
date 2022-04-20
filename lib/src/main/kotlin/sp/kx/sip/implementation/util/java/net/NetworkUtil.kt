package sp.kx.sip.implementation.util.java.net

import java.net.Inet4Address
import java.net.NetworkInterface

fun getHostAddress(): String {
    return NetworkInterface.getNetworkInterfaces().toList()
        .flatMap { it.inetAddresses.toList() }
        .filterIsInstance<Inet4Address>()
        .filterNot { it.isLoopbackAddress }
        .single().hostAddress
}

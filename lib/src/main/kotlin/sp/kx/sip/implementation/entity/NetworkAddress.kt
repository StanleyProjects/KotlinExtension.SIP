package sp.kx.sip.implementation.entity

import sp.kx.sip.foundation.entity.NetworkAddress

private data class NetworkAddressImpl(override val host: String, override val port: Int) : NetworkAddress

fun address(host: String, port: Int): NetworkAddress {
    return NetworkAddressImpl(host = host, port = port)
}
